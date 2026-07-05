# LifeManager

App Android (futuro: desktop) offline-first per la gestione della vita
personale: wearable e parametri corporei, dieta e idratazione, lavoro
(calendario + to-do), finanze. Dashboard modulare stile Notion.

## Documentazione
- Piano di implementazione: docs/implementation-plan.md (sezioni 1–4 vincolanti)
- WP corrente: docs/current-wp.md
- Assegnazione strumenti: docs/tooling.md
- Decisioni: docs/decisions/

## Setup ambiente (Windows)
1. JDK 17 (Temurin) — `java -version` deve rispondere 17.x
2. Android Studio + SDK, variabile ANDROID_HOME (o `local.properties` → `sdk.dir`) impostata
   - Platform richiesta: compileSdk 37, minSdk 26 (vedi ADR-001 per il perché
     di 37 e non 36)
3. Verifica: `./gradlew build` da terminale deve essere verde

## Comandi utili
- `./gradlew build` — build completa di tutti i moduli
- `./gradlew test` — unit test di tutti i moduli (include Konsist)
- `./gradlew :konsist-tests:test` — solo i test di architettura (regole di dipendenza §3.1)
- `./gradlew :app:installDebug` — installa l'app su device/emulatore connesso

## Struttura moduli
Vedi `docs/implementation-plan.md` §3.1. In sintesi:
- `:app` — entry point Android, registry statico dei `FeatureModule`, navigazione root
- `:core:dashboard-api` — contratti `FeatureModule` / `DashboardCardProvider` (zero dipendenze feature)
- `:core:common` — `Result`, `DispatcherProvider`, `BackgroundScheduler`
- `:core:designsystem` — tema Notion-style e componenti condivisi (WP-1.1)
- `:core:database` — RoomDatabase, migrazioni, cifratura (WP-1.2)
- `:core:sync` — backup/export/import cifrato (WP-6.1)
- `:feature:*` — una feature per modulo (finance, diet, work, wearable)
- `:konsist-tests` — test di architettura che fanno rispettare le regole di dipendenza tra moduli

## Sviluppo
Il progetto è sviluppato da agent LLM (Claude Code, Codex, Antigravity 2.0)
secondo docs/tooling.md. Ogni WP vive su un branch `wp/<n>-<nome>` e viene
integrato solo via PR con CI verde.
