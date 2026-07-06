# ADR-003 — Room KMP + SQLCipher: artefatto, percorso di integrazione e punto di estensione multi-modulo

- Stato: accettato
- Data: 2026-07-06
- WP: WP-1.2
- Autore: Claude Code (Agente Data)

## Contesto

implementation-plan.md §2 impone "Room KMP + SQLCipher" e §7 segnala un rischio
esplicito: attriti d'integrazione tra la nuova architettura a driver di Room
2.7+ (pensata per KMP) e le librerie di cifratura SQLCipher, con fallback
possibile a SQLDelight (decisione Architect via ADR).

Un secondo problema, non esplicitato in implementation-plan.md: Room richiede
la classe `@Database(entities = [...])` concreta in un unico punto di
compilazione. `core:*` non può MAI dipendere da `feature:*` (§3.1), quindi
`:core:database` non può conoscere le entità delle feature (finanza, dieta,
lavoro, wearable) che arriveranno nei WP successivi. Serve un design che
permetta a ogni feature di "aggiungere le proprie migrazioni nel punto di
estensione dedicato in :core:database" (CLAUDE.md punto 2) senza che
`:core:database` dipenda da loro.

## Spike eseguito

Verificato che Room 2.8.4 (KMP, target `android`) accetta ancora il percorso
legacy `RoomDatabase.Builder.openHelperFactory(SupportSQLiteOpenHelper.Factory)`
sul target Android, non sostituito dalla nuova API `SQLiteDriver` (quella è
opt-in, necessaria solo per condividere l'implementazione DB tra più target
KMP contemporaneamente — qui `desktop` resta uno stub non sviluppato fino a
FASE 7). `./gradlew :core:database:compileAndroidMain
:core:database:compileKotlinDesktop :core:database:compileKotlinMetadata`
verde con `net.zetetic:sqlcipher-android:4.16.0` +
`net.zetetic.database.sqlcipher.SupportOpenHelperFactory`.

## Opzioni considerate (artefatto SQLCipher)

1. **`net.zetetic:android-database-sqlcipher`** (storico) — deprecato
   ufficialmente in favore di (2).
2. **`net.zetetic:sqlcipher-android` 4.16.0** — successore attivo, stessa API
   concettuale (`SupportOpenHelperFactory`), richiede `androidx.sqlite:sqlite`
   esplicito.
3. **Fallback SQLDelight** (previsto da §7 se Room+SQLCipher non fosse
   integrabile) — non necessario: lo spike non ha incontrato attriti
   bloccanti.

## Decisione (artefatto + percorso)

Opzione 2. Versione dichiarata in `gradle/libs.versions.toml`
(`sqlcipher = "4.16.0"`, più `androidxSqlite = "2.6.2"` richiesto a runtime).
Nessun fallback a SQLDelight: non necessario.

## Decisione (punto di estensione multi-modulo)

`:core:database` fornisce solo pezzi generici, mai la classe `@Database`
concreta:

- `buildEncryptedRoomDatabase<T>(context, klass, name, passphrase, migrations)`
  — il "builder centrale" richiamato da §5 WP-1.2: incapsula
  `Room.databaseBuilder(...).openHelperFactory(SupportOpenHelperFactory(...))`,
  generico su `KClass<T : RoomDatabase>`.
- `FeatureMigrationProvider` (fun interface) — l'estensione per le
  migrazioni: ogni feature implementa l'interfaccia e la registra nel proprio
  modulo Koin (`single { ... } bind FeatureMigrationProvider::class`). Nessun
  file condiviso da modificare per aggiungere una migrazione (mitiga il
  rischio "conflitti di merge su file condivisi" di §7).
- `BaseEntity` (convenzioni §3.3) e `InstantConverters` (converter comune
  epoch millis UTC ↔ `kotlin.time.Instant`).
- `DatabasePassphraseManager` e `DatabaseBackupManager` (dettagliati più
  sotto).

La classe `@Database` concreta con `entities = [...]` che elenca le tabelle
di tutte le feature dovrà essere assemblata in `:app` (unico modulo che
dipende da tutte le feature, §3.1), a partire dal primo WP feature (WP-2.1).
Quel WP dovrà: importare le entità/DAO delle feature disponibili, collezionare
i `FeatureMigrationProvider` via Koin (`getKoin().getAll<FeatureMigrationProvider>()`),
e chiamare `buildEncryptedRoomDatabase`. Questo non è esplicitato testualmente
in implementation-plan.md §5 WP-1.2/WP-2.1: lo fissiamo qui perché altrimenti
ogni agente feature reinventerebbe un design diverso.

## Problemi runtime trovati eseguendo i test su device reale

Lo spike iniziale (solo compilazione) non li ha rilevati; sono emersi solo
eseguendo `androidDeviceTest` su un device reale connesso:

1. **`@PrimaryKey` non ereditato tramite `override`.** Kotlin non propaga le
   annotazioni attraverso `override`: `@PrimaryKey` dichiarato sul parametro
   di `BaseEntity` (classe astratta comune, §3.3) non viene visto da KSP
   sulle entità concrete (`override val id: String`). Errore: "An entity
   must have at least 1 property annotated with @PrimaryKey". Fix: ogni
   `@Entity` concreta deve ridichiarare `@PrimaryKey override val id: String`
   — documentato nel KDoc di `BaseEntity`. Vincolo Kotlin/KSP, non
   implementation-plan.md.
2. **`net.zetetic:sqlcipher-android` non carica la libreria nativa da solo.**
   A differenza del vecchio `android-database-sqlcipher`, serve
   `System.loadLibrary("sqlcipher")` esplicito prima di aprire una connessione
   — altrimenti `UnsatisfiedLinkError` su `nativeOpen`. Fatto una volta sola
   (`by lazy`) in `buildEncryptedRoomDatabase`.
3. **Packaging nativo.** Aggiunto `packaging { jniLibs.useLegacyPackaging = true }`
   in `core/database/build.gradle.kts` come misura precauzionale verso lo
   stesso sintomo (`UnsatisfiedLinkError`) documentato nella community
   SQLCipher per il packaging non-legacy (uncompressed/page-aligned) di AGP;
   il fix (2) da solo si è dimostrato sufficiente nei test eseguiti, ma la
   community riporta casi in cui serve anche questo, quindi resta.
4. **`DocumentFile.createFile()` rinomina il file.** Alcuni backend SAF
   appendono l'estensione associata al MIME type passato, anche se il nome
   richiesto la ha già (osservato: `application/octet-stream` → aggiunta
   `.bin`, quindi `roundtrip.db` diventava `roundtrip.db.bin`, rompendo la
   lookup per nome esatto in fase di import). Fix: `DatabaseBackupManager`
   usa sempre il MIME type generico `"*/*"` (nessuna estensione registrata,
   nessun rename) per i file di backup.

## Conseguenze

- WP-2.1 (finance, template) dovrà seguire questo design per primo: crea la
  vera classe `@Database` in `:app`, non in `:feature:finance`.
- `androidx.security.crypto:1.1.0` (`EncryptedSharedPreferences`/`MasterKey`)
  usato per la passphrase è ufficialmente deprecato (soft-deprecation, nessuna
  rimozione annunciata) in favore di DataStore + Tink. Non migriamo ora:
  Tink non è nel version catalog e introdurlo qui sarebbe fuori perimetro per
  WP-1.2. Tracciato in `docs/backlog.md`.
- Test di cifratura reali (SQLCipher ships librerie native solo Android)
  vivono in `androidDeviceTest` (device/emulatore), non `androidHostTest`
  (Robolectric non carica le `.so` di SQLCipher sull'host JVM).
