# :core:common

Utility condivise senza dipendenze da feature o da altri moduli `core`.

## Contenuto
- `Result<T>` — sealed interface `Success`/`Error` + `map`/`onSuccess`/`onError`.
- `DispatcherProvider` — `main`/`default`/`io`/`unconfined` iniettabili
  (mai `Dispatchers.IO` hardcoded nei repository, per testabilità).
  Implementazione Android: `AndroidDispatcherProvider`.
- `BackgroundScheduler` — astrae WorkManager dietro un'interfaccia comune
  (`schedulePeriodic`/`scheduleOneOff`/`cancel`) così le feature non
  dipendono mai da WorkManager direttamente. Implementazione Android:
  `WorkManagerBackgroundScheduler`, sempre con `uniqueName` + constraint
  espliciti (mai polling, per il vincolo batteria di implementation-plan.md
  §1). `PeriodicWorkRequest`/`OneOffWorkRequest` portano un `workerClass:
  KClass<*>` (deve restare multiplatform-safe in `commonMain`); l'impl
  Android valida a runtime che la classe estenda `CoroutineWorker`
  (`require` con messaggio esplicito) prima di castare, per non lasciare
  un `ClassCastException` criptico a una feature che passa la classe
  sbagliata.

## Stack
Kotlin Multiplatform (target `android` + `desktop` dichiarato, senza
sorgenti). Test JVM/Android abilitati esplicitamente
(`withHostTestBuilder`, richiesto dal nuovo plugin `com.android.kotlin.
multiplatform.library` — vedi ADR-001) con kotlin.test + Turbine.
