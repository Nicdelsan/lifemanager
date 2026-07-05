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
`core:designsystem`, `core:common`, `core:dashboard-api`. Nessuna dipendenza
diretta da `core:database` o dalle feature (isolamento garantito dal
registry + Konsist, vedi `:konsist-tests`).

## Non ancora fatto
- Riordino/nascondi card, persistenza layout (WP-4.1).
- Blocco biometrico, FLAG_SECURE (WP-5.1).
