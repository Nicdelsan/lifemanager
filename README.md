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
2. Android Studio + SDK, variabile ANDROID_HOME impostata
3. Verifica: `./gradlew build` da terminale deve essere verde

## Sviluppo
Il progetto è sviluppato da agent LLM (Claude Code, Codex, Antigravity 2.0)
secondo docs/tooling.md. Ogni WP vive su un branch `wp/<n>-<nome>` e viene
integrato solo via PR con CI verde.
