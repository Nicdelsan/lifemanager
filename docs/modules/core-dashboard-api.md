# :core:dashboard-api

Contratti condivisi tra `:app` e ogni `:feature:*` (implementation-plan.md
§3.2). **Congelato**: modificabile solo dall'Agente Architect; una feature
che scopre un limite del contratto scrive una proposta in
`docs/decisions/PROPOSAL-<n>.md` invece di modificarlo (regola operativa §4.2).

## Contenuto
- `FeatureModule` — identità (`id`), presentazione (`displayName`, `icon`),
  DI (`koinModule`), card dashboard (`dashboardCards()`) e grafo di
  navigazione (`navGraph`).
- `DashboardCardProvider` — una card autosufficiente (`cardId`,
  `defaultSize`, `Card(onNavigate)`); ogni feature gestisce da sé i propri
  stati Loading/Empty/Content/Error.
- `CardSize` — `SMALL | MEDIUM | LARGE`.

## Stack
Kotlin Multiplatform (target `android` + `desktop` dichiarato, senza
sorgenti). Dipende da Compose Multiplatform (runtime/foundation/material3 +
compose resources per `StringResource`), Navigation Compose (multiplatform)
e Koin core (per il tipo `Module`). Zero dipendenze da moduli feature.
