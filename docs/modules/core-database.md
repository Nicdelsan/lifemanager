# :core:database

RoomDatabase KMP cifrato (SQLCipher) + convenzioni schema §3.3 + punto di
estensione migrazioni per le feature. Non contiene nessuna classe `@Database`
concreta: quella con `entities = [...]` di tutte le feature va assemblata in
`:app` (unico modulo che può dipendere da tutte le feature, §3.1) — vedi
ADR-003.

## Convenzioni schema (§3.3)

`BaseEntity` (`commonMain`) fissa i quattro campi obbligatori di ogni tabella:
`id` (UUID v4 string, mai autoincrement), `createdAt`/`updatedAt`/`deletedAt`
(epoch millis UTC, `Long`/`Long?`).

**Importante**: Kotlin non propaga le annotazioni attraverso `override`, quindi
ogni `@Entity` concreta deve ridichiarare `@PrimaryKey override val id: String`
— ereditare solo il campo da `BaseEntity` non basta, KSP rifiuta l'entità
altrimenti ("must have at least 1 property annotated with @PrimaryKey",
trovato empiricamente in questo WP). Esempio minimo:

```kotlin
@Entity(tableName = "finance_transaction")
data class FinanceTransactionEntity(
    @PrimaryKey override val id: String,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val deletedAt: Long?,
    val amountCents: Long,
) : BaseEntity(id, createdAt, updatedAt, deletedAt)
```

`InstantConverters` converte `kotlin.time.Instant` ↔ `Long` per i DAO che
preferiscono lavorare con `Instant` invece del `Long` grezzo (le colonne
restano sempre `Long` — la conversione è solo per la firma dei metodi DAO).
Va registrato esplicitamente con `@TypeConverters(InstantConverters::class)`
dove serve.

## Database cifrato

`buildEncryptedRoomDatabase<T>(context, klass, databaseName, passphrase, migrations)`
(`androidMain`) è il "builder centrale": incapsula
`Room.databaseBuilder(...).openHelperFactory(SupportOpenHelperFactory(passphrase))`
(`net.zetetic:sqlcipher-android`). Ogni feature/WP-2.1+ lo richiama dal punto
in cui assembla la propria/la concreta `@Database`. Vedi ADR-003 per la scelta
dell'artefatto SQLCipher e i problemi runtime trovati (native lib da caricare
esplicitamente, packaging, ecc.).

`DatabasePassphraseManager` genera una passphrase casuale a 32 byte al primo
avvio e la persiste in `EncryptedSharedPreferences` (chiave AES-256-GCM in
Android Keystore via `MasterKey`). Nota: `androidx.security.crypto` è
soft-deprecato (vedi `docs/backlog.md`); non prevista un fix ora nel perimetro
di WP-1.2.

## Estensione migrazioni

`FeatureMigrationProvider` (`commonMain`, `fun interface`) è il punto di
estensione per le migrazioni: una feature implementa l'interfaccia e la
registra nel proprio modulo Koin (`single { ... } bind FeatureMigrationProvider::class`).
Chi assembla la `@Database` concreta (`:app`) colleziona tutti i provider
bindati (`getKoin().getAll<FeatureMigrationProvider>()`), appiattisce la lista
di `Migration` e la passa a `buildEncryptedRoomDatabase`. Nessuna feature
modifica un file condiviso per registrare una migrazione.

## Export/import locale

`DatabaseBackupManager` (`androidMain`):
- `export(database, databaseFile, settings, destination: DocumentFile)` —
  chiude `database`, copia il file cifrato e un JSON delle impostazioni
  (`SettingsSnapshot`) nella cartella `destination` (risolta dal chiamante
  via SAF, `DocumentFile.fromTreeUri` dopo il picker "apri cartella").
- `import(source: DocumentFile, destinationDatabaseFile, settings, existingDatabase?)` —
  operazione inversa; chiude `existingDatabase` se il file di destinazione è
  già aperto.

`SettingsSnapshot`/`SettingsEntry` (`androidMain`) serializzano un
`DataStore<Preferences>` in JSON con un tag di tipo esplicito per entry
(`STRING`/`INT`/`LONG`/`FLOAT`/`BOOLEAN`/`STRING_SET`), perché `Preferences`
cancella il tipo su disco.

I file di backup usano il MIME type generico `"*/*"`: alcuni backend SAF
appendono l'estensione associata al MIME type passato a `createFile()` anche
quando il nome ce l'ha già (osservato: `application/octet-stream` →
`<nome>.bin`), rompendo la lookup per nome esatto in fase di import — vedi
ADR-003.

## Test

I test di cifratura reale vivono in `androidDeviceTest` (device/emulatore),
non in un host test Robolectric: SQLCipher ha librerie native solo Android,
non caricabili su una JVM host. Copertura:
- `EncryptedDatabaseTest` — file illeggibile senza passphrase (apertura SQLite
  raw fallisce), leggibile con quella corretta.
- `BackupRoundTripTest` — round-trip export → wipe locale → import, DB e
  settings entrambi ripristinati.
- `MigrationExtensionPointTest` — un `FeatureMigrationProvider` applicato
  tramite `buildEncryptedRoomDatabase` sopravvive a un upgrade di versione.
- `DatabasePassphraseManagerTest` — la passphrase è stabile tra istanze.

Eseguiti con `./gradlew :core:database:connectedAndroidDeviceTest` su device
reale connesso.

## Decisioni

- ADR-003 — artefatto SQLCipher (`net.zetetic:sqlcipher-android`), percorso
  di integrazione con Room KMP (`openHelperFactory` legacy, non il nuovo
  `SQLiteDriver`), design del punto di estensione multi-modulo, problemi
  runtime trovati eseguendo i test su device reale.
