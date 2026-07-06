# Backlog — fuori scope, non implementare senza decisione esplicita

- Sync bidirezionale multi-device (Livello 2) — schema DB già predisposto (§3.3)
- Samsung Health SDK nativo per lo stress score proprietario
- Target iOS / Web WASM
- Widget home screen Android, app companion Wear OS
- Import estratti conto bancari (CSV/OFX)

## Emerse durante WP-0.1
- `com.android.kotlin.multiplatform.library` (nuovo plugin KMP Android
  richiesto da AGP 9, vedi ADR-001) supporta una sola variante di build
  (niente debug/release flavor) per `core:*`/`feature:*`. Se una feature
  futura avesse bisogno di varianti (es. mock di Health Connect in debug),
  va rivalutata l'architettura del modulo interessato — non risolvere
  aggirando il vincolo con hack locali.
- Compose preview/tooling per i moduli KMP Android (dipendenza
  `androidRuntimeClasspath`) non ancora cablata: serve a WP-1.1 per il
  catalogo componenti in debug.
- Test negativo automatico (Gradle TestKit) che dimostri in CI il
  fallimento di `feature:*->feature:*` e `core:*->feature:*`. Il check in
  `build.gradle.kts` root (REVIEW-WP-0.1, punto 2) è stato verificato solo
  manualmente (dipendenza vietata aggiunta e rimossa a mano). Un test
  TestKit che lancia una build di progetto sintetico con la violazione e
  ne asserisce il fallimento darebbe copertura reale in CI.

## Emerse durante WP-1.2
- `androidx.security.crypto:1.1.0` (`EncryptedSharedPreferences`/`MasterKey`),
  usato in `:core:database` per la passphrase SQLCipher, è ufficialmente
  deprecato (soft, nessuna rimozione annunciata) in favore di
  DataStore + Tink + Android Keystore. Migrazione non fatta in WP-1.2 (Tink
  non è nel version catalog, fuori perimetro); rivalutare con ADR quando si
  tocca di nuovo questo modulo. Vedi ADR-003.
