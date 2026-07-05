# :app

Entry point Android. Nessuna logica di dominio: compone le feature registrate
nel `FeatureModuleRegistry` e ospita la dashboard root.

## Contenuto (WP-0.1)
- `LifeManagerApplication` — bootstrap Koin (`startKoin`), carica i
  `koinModule` di ogni `FeatureModule` registrato.
- `MainActivity` — un solo `NavHost` root; applica `LifeManagerTheme` e
  registra il grafo di navigazione della dashboard più quello di ogni feature
  (`FeatureModule.navGraph`).
- `FeatureModuleRegistry` — `listOf<FeatureModule>()` statico. **Unico punto
  di estensione** per gli agenti feature: registrare qui la propria istanza
  è l'unica modifica ammessa fuori dal proprio modulo (vedi
  `docs/current-wp.md` e regole operative in `implementation-plan.md` §4.1).
- `dashboard/` — `DashboardScreen` + `DashboardViewModel` (MVVM, `StateFlow`
  di `DashboardUiState` sealed). Con registry vuoto mostra solo lo stato
  Empty. Il rendering delle card (`DashboardUiState.Content`) arriva con la
  prima feature (WP-2.1).

## Dipendenze
Tutti i moduli `core:*` e `feature:*` (implementation-plan.md §3.1: "app
dipende da tutto ed è l'unico punto in cui le feature vengono composte"),
dichiarate fin da WP-0.1 anche per i moduli ancora vuoti. Così un agente
feature, quando implementa la propria feature, deve toccare solo il proprio
modulo più la riga di registrazione in `FeatureModuleRegistry` — mai
`app/build.gradle.kts`. L'isolamento tra feature è garantito dal registry
(nessun modulo feature dipende da un altro) più due controlli automatici:
il test Konsist (`:konsist-tests`, a livello di import/package) e il check
di dipendenze Gradle reali in `build.gradle.kts` root (a livello di grafo
dei moduli).

## Verifica runtime (WP-0.1)
Installata via `./gradlew :app:installDebug` e avviata via
`adb shell am start` sul dispositivo di riferimento (Galaxy S25 Ultra,
`SM-S938B`): dashboard vuota renderizzata correttamente (titolo + stato
Empty in italiano), nessun crash in logcat.

## Non ancora fatto
- Riordino/nascondi card, persistenza layout (WP-4.1).
- Blocco biometrico, FLAG_SECURE (WP-5.1).
