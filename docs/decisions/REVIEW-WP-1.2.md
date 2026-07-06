# REVIEW WP-1.2 - Database core

- Branch revisionato: `wp/1.2-database`
- Base richiesta: `main`
- Nota base diff: il `main` locale e `origin/main` risultano indietro rispetto al bootstrap WP-0.1; per isolare il lavoro WP-1.2 ho verificato anche il diff `af580ae..HEAD` (ultimo commit WP-0.1 locale -> `08c7215`).
- Comandi eseguiti:
  - `./gradlew.bat build` -> verde
  - `./gradlew.bat test` -> verde
  - `./gradlew.bat :core:database:connectedAndroidDeviceTest` -> verde su `SM-S938B - 16` (5 test)

## Violazioni delle convenzioni (§3)

Nessuna violazione bloccante delle regole di dipendenza o dei contratti condivisi:

- Il lavoro WP-1.2 e' confinato a `:core:database`, con sole modifiche ammesse a documentazione/backlog e version catalog.
- Le nuove dipendenze sono dichiarate in `gradle/libs.versions.toml` e giustificate da `docs/decisions/ADR-003-room-sqlcipher-extension-point.md`.
- Le convenzioni DB §3.3 sono documentate in `BaseEntity` e in `docs/modules/core-database.md`.

## Voci della Definition of Done NON soddisfatte

- **RoomDatabase KMP con SQLCipher**: soddisfatta per il target Android tramite `buildEncryptedRoomDatabase` + SQLCipher. La scelta di non definire una classe `@Database` concreta in `:core:database` e' motivata da ADR-003.
- **Passphrase in Android Keystore**: parzialmente soddisfatta. La passphrase e' protetta da `EncryptedSharedPreferences`/`MasterKey`, ma la generazione non e' atomica in caso di primo accesso concorrente. Vedi bug P1 sotto.
- **BaseEntity/convenzioni §3.3, converter comuni, punto estensione DAO/migrazioni**: soddisfatta.
- **Export/import locale DB cifrato + settings JSON**: parzialmente soddisfatta. Il round-trip base passa, ma la serializzazione dei `StringSet` puo' perdere dati validi. Vedi bug P2 sotto.
- **Test round-trip export->import**: presente e verde in `connectedAndroidDeviceTest`.
- **Test cifratura file non leggibile senza chiave**: presente e verde in `connectedAndroidDeviceTest`.
- **Documentazione `docs/modules/core-database.md` aggiornata**: soddisfatta.

## Bug o rischi individuati

1. **P1 - Race nella creazione iniziale della passphrase SQLCipher**
   - File: `core/database/src/androidMain/kotlin/com/lifemanager/database/DatabasePassphraseManager.kt:28`
   - Evidenza: `getOrCreatePassphrase()` legge `KEY_PASSPHRASE`, genera una nuova `ByteArray`, poi salva con `apply()` senza sincronizzazione (`:28-36`).
   - Rischio: due chiamate concorrenti al primo avvio possono entrambe leggere `null`, generare passphrase diverse e restituirle a due caller. Se il caller A crea il DB con la passphrase A ma il caller B persiste per ultimo la passphrase B, le aperture successive useranno B e il DB creato con A diventera' illeggibile.
   - Correzione richiesta: rendere la sezione read-generate-write atomica per processo e usare persistenza sincrona (`commit()` o equivalente gestito) per materiale critico prima di restituire la chiave.

2. **P2 - Export/import DataStore `StringSet` lossy**
   - File: `core/database/src/androidMain/kotlin/com/lifemanager/database/SettingsSnapshot.kt:24`
   - Evidenza: `SettingsEntry` salva ogni valore come singola `String`; i set sono serializzati con `joinToString(STRING_SET_DELIMITER)` (`:38`) e ricostruiti con `split(...).toSet()` (`:56-57`).
   - Rischio: un `StringSet` contenente `""` viene ripristinato come `emptySet()`; un valore contenente il delimitatore NUL viene diviso in piu' valori. Sono valori stringa validi per un `Preferences` string set, quindi l'export JSON non e' lossless.
   - Correzione richiesta: rappresentare i valori come tipo strutturato serializzabile, ad esempio `value: JsonElement` o campi separati (`stringValue`, `stringSetValue: List<String>`), senza delimiter custom.

## Test mancanti o insufficienti

- Manca un test di primo accesso concorrente per `DatabasePassphraseManager.getOrCreatePassphrase()`.
- Manca un test che dimostri che la passphrase appena generata e' persistita in modo affidabile prima dell'uso.
- Manca copertura sugli edge case di `SettingsSnapshot` per `StringSet`: set con stringa vuota, valori contenenti il delimitatore, round-trip di tutti i tipi supportati.
- `./gradlew test` resta verde ma non esegue i test instrumented WP-1.2; la verifica reale di cifratura/export richiede `:core:database:connectedAndroidDeviceTest`.

## Verdetto finale

**REQUEST CHANGES**

Priorita' correzioni:

1. P1: rendere atomica e durabile la creazione iniziale della passphrase.
2. P2: rendere lossless la serializzazione JSON dei `StringSet`.
3. Aggiungere i test mancanti sopra e rieseguire `build`, `test`, `:core:database:connectedAndroidDeviceTest`.
