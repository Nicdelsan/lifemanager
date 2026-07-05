# Review WP-0.1 - Bootstrap del progetto

WP revisionato: WP-0.1  
Branch revisionato: `wp/0.1-bootstrap`  
Base diff usata: `main...HEAD` locale. Nota: `origin/main...HEAD` mostra solo `docs/backlog.md`, perché il riferimento remoto sembra già contenere gran parte del branch.

## Violazioni delle convenzioni (§3: struttura moduli, contratti, schema DB, stile)

1. **`REQUEST CHANGES` - `:app` non dipende da tutte le feature/core previste da §3.1.**  
   `docs/implementation-plan.md:59-62` rende vincolante che `app` dipenda da tutto e sia l'unico punto di composizione delle feature. Invece `app/build.gradle.kts:45-48` dipende solo da `:core:designsystem`, `:core:common`, `:core:dashboard-api`; `docs/modules/app.md:21-24` documenta esplicitamente l'assenza di dipendenze dirette da feature e `core:database`. Questo contraddice il contratto operativo per gli agenti feature: `docs/implementation-plan.md:115-117` consente loro solo la registrazione del `FeatureModule` in `:app`, ma senza dipendenza Gradle preesistente la registrazione di una feature concreta richiederà anche una modifica a `app/build.gradle.kts`.

2. **`REQUEST CHANGES` - Il test Konsist non verifica davvero le regole di dipendenza Gradle di §3.1.**  
   La regola vincolante è sui moduli Gradle (`feature:*` mai verso altre feature, `core:*` mai verso feature; `docs/implementation-plan.md:59-62`). Il test in `konsist-tests/src/test/kotlin/com/lifemanager/konsist/ArchitectureTest.kt:18-54` usa `scopeFromProject().assertArchitecture` su layer di package Kotlin. Questo intercetta import/uso nel sorgente, ma non fallisce se un modulo dichiara una dipendenza Gradle vietata e non la usa ancora nel codice. Quindi la CI ha un test attivo, ma la copertura non garantisce la regola richiesta dal WP-0.1.

3. **Disallineamento documentazione/setup su compile SDK.**  
   `README.md:15-16` indica platform richiesta `compileSdk 36`, mentre i moduli usano `compileSdk = 37` e `targetSdk = 37` (`app/build.gradle.kts:8-13`, ad esempio). La build locale passa, quindi non è un blocco tecnico immediato, ma la documentazione di setup non descrive fedelmente il branch.

## Voci della Definition of Done NON soddisfatte

- **Build verde:** soddisfatta. `.\gradlew.bat build` è passato con `BUILD SUCCESSFUL` dopo avere consentito il download della distribuzione Gradle. Primo tentativo fallito solo per sandbox/network (`Permission denied: getsockopt`).
- **Test verdi:** soddisfatta. `.\gradlew.bat test` è passato con `BUILD SUCCESSFUL`.
- **App avviabile con dashboard vuota:** non verificata runtime. La build produce l'APK e il codice ha `MainActivity`, `NavHost`, registry vuoto e stato empty (`FeatureModuleRegistry.kt:10-12`, `DashboardScreen.kt:45-55`), ma non ho potuto avviare su device/emulatore: `adb devices -l` fallisce perché `adb` non è disponibile sul PATH della shell corrente e non esiste `scripts/env.ps1` in questo checkout.
- **Konsist test attivi:** parzialmente soddisfatta. Il job CI esegue `./gradlew :konsist-tests:test` (`.github/workflows/ci.yml:25-26`) e il test passa, ma non copre correttamente le dipendenze Gradle vietate come richiesto da §3.1.
- **README con istruzioni setup:** parzialmente soddisfatta. Esiste e contiene setup/comandi, ma riporta `compileSdk 36` mentre il progetto usa `compileSdk 37`.

## Bug o rischi individuati

1. **Futuri WP feature rischiano di uscire dal perimetro autorizzato.**  
   A causa della mancanza di dipendenze `:app -> :feature:*` in `app/build.gradle.kts:45-48`, un agente feature non potrà registrare una propria istanza in `FeatureModuleRegistry.kt:10-12` senza aggiungere anche una dipendenza Gradle a `:app`. Questo entra in tensione con `docs/implementation-plan.md:115-117`, che ammette solo la registrazione nel registry fuori dal modulo feature.

2. **Falso senso di sicurezza sui vincoli architetturali.**  
   `ArchitectureTest.kt:18-54` dichiara di far rispettare le regole di dipendenza tra moduli, ma in pratica analizza relazioni tra package/import. Una dipendenza Gradle vietata può essere introdotta e restare invisibile al test finché non viene importato un simbolo nel codice.

3. **`BackgroundScheduler` può fallire a runtime con worker non validi.**  
   `BackgroundScheduler.kt` accetta `KClass<*>`, mentre `WorkManagerBackgroundScheduler.kt:22-24` e `:38` fanno cast a `Class<out CoroutineWorker>`. Se una feature passa una classe non `CoroutineWorker`, l'errore sarà un `ClassCastException` runtime. Non blocca WP-0.1, ma merita test o un contratto più vincolante prima dei WP che useranno scheduler.

## Test mancanti o insufficienti

- Test architetturale sulle dipendenze Gradle reali tra project/module, non solo sui package Kotlin.
- Test negativo per dimostrare che `feature:* -> feature:*` e `core:* -> feature:*` falliscono in CI.
- Test/unit o contract test per `WorkManagerBackgroundScheduler` sui vincoli minimi: classe worker valida, constraints, unique work policy.
- Verifica runtime dell'app su device/emulatore con dashboard vuota. Non eseguita perché `adb` non è disponibile in questa shell.

## Verdetto finale

**REQUEST CHANGES**

Priorità correzioni:

1. Allineare `:app` alla regola vincolante di §3.1 e alla modalità operativa degli agenti feature: decidere e documentare chiaramente se `:app` deve avere dipendenze Gradle predefinite verso tutte le feature skeleton, oppure aggiornare il contratto operativo tramite decisione Architect prima di procedere.
2. Rendere il test Konsist/CI capace di verificare le dipendenze Gradle effettive tra moduli, o aggiungere un test dedicato equivalente.
3. Allineare README/current setup a `compileSdk 37` oppure riportare i moduli a `compileSdk 36`.
4. Eseguire una verifica di avvio reale dell'app su device/emulatore e annotarne l'esito.
