# LifeManager — Implementation Plan per Agent LLM

> Documento operativo per lo sviluppo multi-agente. Ogni agente riceve questo documento
> completo come contesto + il proprio Work Package (WP). Le sezioni 1–4 sono vincolanti
> per tutti gli agenti e non possono essere modificate senza passare dall'Agente Architect.

---

## 1. Contesto e vincoli di prodotto

**LifeManager** è un'app Android (futuro: desktop) per la gestione della vita personale a 360°.

Vincoli non negoziabili:
- **Offline-first locale**: tutti i dati risiedono sul dispositivo. Nessun backend proprietario.
- **Performance**: startup < 1.5s a freddo (con baseline profile), UI a 60fps, nessun jank sulla dashboard.
- **Batteria**: zero polling, zero foreground service permanenti. Solo WorkManager con constraint.
- **Design**: stile Notion — superfici piatte e neutre, forte gerarchia tipografica, card, icone per sezione. Material 3 con tema custom, supporto dark mode e dynamic color.
- **Privacy**: database cifrato (SQLCipher), blocco biometrico opzionale, backup cifrati prima di lasciare il dispositivo.
- **Modularità**: ogni funzionalità è un modulo Gradle autonomo, aggiungibile/rimovibile senza toccare gli altri.

## 2. Stack tecnologico (vincolante)

| Area | Scelta | Note |
|---|---|---|
| Linguaggio | Kotlin (ultima stable) | |
| Struttura | Kotlin Multiplatform | Target attivo: `android`. Target `desktop` (JVM) predisposto ma non sviluppato nelle fasi 1–6 |
| UI | Compose Multiplatform + Material 3 | Codice UI in `commonMain` dove possibile |
| Database | Room KMP + SQLCipher | Un solo file SQLite = unità di backup/export |
| DI | Koin | Un modulo Koin per feature |
| Charting | Vico | Fallback: Koala Plot se Vico multiplatform non copre un caso |
| Background | WorkManager (androidMain) | Astratto dietro interfaccia `BackgroundScheduler` in common |
| Salute | Health Connect (androidMain) | NON Samsung Health SDK (richiede partnership). Stress derivato da HRV in post-processing |
| Calendario | CalendarProvider (androidMain) | Lettura/scrittura eventi di sistema; to-do in Room |
| Cibo | Open Food Facts API (opzionale) | Ktor client, cache locale aggressiva |
| Cloud | Google Drive `appDataFolder` | Solo snapshot cifrato del DB. Fase 6 |
| Serializzazione | kotlinx.serialization | |
| Date/ore | kotlinx-datetime | Timestamp sempre UTC epoch millis nel DB |
| Test | kotlin.test + Turbine + Robolectric (dove serve) | |

Versioni: gestite SOLO in `gradle/libs.versions.toml` (version catalog). Nessun agente dichiara versioni inline.

## 3. Architettura e contratti condivisi

### 3.1 Struttura moduli Gradle

```
:app                          → entry point Android: navigazione root, DI bootstrap, dashboard host
:core:designsystem            → tema Notion-style, componenti riusabili (LmCard, LmSection, LmEmptyState…)
:core:database                → RoomDatabase, DAO base, migrazioni, cifratura
:core:common                  → Result wrapper, dispatchers, utilities, BackgroundScheduler
:core:dashboard-api           → contratto FeatureModule + DashboardCard (SOLO interfacce, zero dipendenze feature)
:core:sync                    → export/import/backup (fase 6)
:feature:finance
:feature:diet
:feature:work
:feature:wearable
```

Regole di dipendenza (vincolanti, da far rispettare anche via Konsist test in CI):
- `feature:*` può dipendere da `core:*`, MAI da un'altra feature.
- `core:*` non dipende MAI da `feature:*`.
- `app` dipende da tutto ed è l'unico punto in cui le feature vengono composte.

### 3.2 Contratto FeatureModule (in `:core:dashboard-api`)

```kotlin
interface FeatureModule {
    val id: String                        // es. "finance" — stabile, mai riusato
    val displayName: StringResource
    val icon: ImageVector
    val koinModule: Module                // dipendenze della feature
    fun dashboardCards(): List<DashboardCardProvider>
    fun navGraph(builder: NavGraphBuilder, navController: NavController)
}

interface DashboardCardProvider {
    val cardId: String                    // es. "finance.monthly_summary"
    val defaultSize: CardSize             // SMALL | MEDIUM | LARGE
    @Composable fun Card(onNavigate: (route: String) -> Unit)
}
```

- La dashboard (`:app`) renderizza le card in una `LazyVerticalStaggeredGrid` riordinabile;
  ordine e visibilità persistiti in DataStore.
- Ogni `Card` deve essere autosufficiente: prende i propri dati via Koin, espone stati
  Loading/Empty/Content/Error usando i componenti di `:core:designsystem`.
- Nessuna card può bloccare la composizione: dati sempre via `StateFlow` + `collectAsStateWithLifecycle`.

### 3.3 Convenzioni schema database (vincolanti per ogni tabella)

Ogni entità DEVE avere:
```kotlin
@PrimaryKey val id: String              // UUID v4 come stringa — MAI autoincrement
val createdAt: Long                     // epoch millis UTC
val updatedAt: Long                     // epoch millis UTC, aggiornato a ogni write
val deletedAt: Long?                    // soft delete; le query di lettura filtrano deletedAt IS NULL
```

Motivo: prepara la sync bidirezionale futura (Livello 2) senza migrazioni traumatiche.
- Ogni feature possiede le proprie tabelle con prefisso: `finance_transaction`, `diet_meal`, `work_task`, `wearable_sample`…
- Ogni feature fornisce le proprie `Migration` e le registra nel builder centrale di `:core:database`.
- Nessuna foreign key tra tabelle di feature diverse.

### 3.4 Convenzioni di codice

- Package root: `com.lifemanager.<modulo>`
- MVVM: `Screen` (Composable) → `ViewModel` (StateFlow di un unico `UiState` sealed/immutable) → `Repository` → `Dao`/datasource
- Tutto il lavoro pesante su `Dispatchers.Default`/`IO` iniettati (mai hardcoded — servono per i test)
- Stringhe utente: mai hardcoded, sempre risorse (i18n-ready, lingua base: italiano)
- Commit: Conventional Commits (`feat(finance): …`, `fix(core-db): …`)
- Ogni WP produce anche i test indicati nella propria Definition of Done

## 4. Regole operative per gli agenti

1. **Perimetro**: un agente modifica SOLO i moduli assegnati dal proprio WP. Le uniche
   eccezioni ammesse: registrare il proprio `FeatureModule` in `:app` (una riga nel registry)
   e aggiungere le proprie migrazioni in `:core:database` (punto di estensione dedicato).
2. **Contratti congelati**: le interfacce in `:core:dashboard-api`, le convenzioni DB (§3.3)
   e il version catalog si cambiano solo tramite WP dell'Agente Architect. Se un agente
   feature scopre che il contratto non basta, NON lo modifica: documenta il problema in
   `docs/decisions/PROPOSAL-<n>.md` e si ferma su quel punto.
3. **Build verde**: ogni WP termina con `./gradlew build` e `./gradlew test` verdi. Un WP
   che rompe la build di un altro modulo è fallito.
4. **Documentazione**: ogni WP aggiorna `docs/modules/<modulo>.md` (cosa fa, schema tabelle,
   card esposte, decisioni prese).
5. **Decisioni architetturali**: ogni scelta non ovvia va in `docs/decisions/ADR-<n>.md`
   (formato: contesto, opzioni, scelta, conseguenze).
6. **Niente TODO silenziosi**: funzionalità rimandate → issue esplicita in `docs/backlog.md`.
7. **Librerie nuove**: aggiungere una dipendenza non prevista dallo stack richiede un ADR
   con giustificazione (dimensione, manutenzione, licenza).

---

## 5. Fasi e Work Package

Legenda: **[P]** = parallelizzabile con gli altri WP della stessa fase. Le fasi sono sequenziali; i WP dentro una fase possono girare in parallelo dove indicato.

### FASE 0 — Bootstrap (Agente Architect, 1 WP, nessun parallelismo)

**WP-0.1 — Scheletro del progetto**
- Progetto KMP multi-modulo come da §3.1, version catalog completo, target android attivo + desktop dichiarato ma vuoto
- CI minimale (GitHub Actions: build + test + Konsist per le regole di dipendenza §3.1)
- `:core:dashboard-api` con i contratti §3.2
- `:core:common`: `Result`, dispatchers iniettabili, interfaccia `BackgroundScheduler` + impl Android WorkManager
- `:app`: MainActivity, tema placeholder, dashboard vuota che legge un registry statico `listOf<FeatureModule>()`, navigazione root
- **DoD**: build verde, app avviabile con dashboard vuota, Konsist test attivi, README con istruzioni setup

### FASE 1 — Fondamenta (2 WP paralleli)

**WP-1.1 [P] — Design system Notion-style** (`:core:designsystem`)
- Tema Material 3 custom: palette neutra (superfici bianco/grigio caldo, dark mode grigio-carbone), tipografia con forte gerarchia (Inter o simile), shape leggermente arrotondate, elevazioni quasi assenti (bordi sottili al posto delle ombre)
- Componenti: `LmCard`, `LmSectionHeader` (icona/emoji + titolo), `LmListItem`, `LmEmptyState`, `LmErrorState`, `LmLoadingState`, `LmStatTile` (numero grande + label), `LmTopBar`
- Catalogo componenti: schermata debug (raggiungibile da build debug) che mostra tutti i componenti nei vari stati
- **DoD**: catalogo navigabile, preview Compose per ogni componente, dark mode verificata

**WP-1.2 [P] — Database core** (`:core:database`)
- `RoomDatabase` KMP con SQLCipher, passphrase in Android Keystore
- `BaseEntity`/convenzioni §3.3, converter comuni, punto di estensione per DAO e migrazioni delle feature
- Export/import locale del file DB (per migrazione telefono): funzione che chiude il DB, copia il file cifrato in `Documents/` via SAF, e viceversa; DataStore per le impostazioni esportate in JSON allegato
- **DoD**: test di round-trip export→import, test cifratura (il file non è leggibile senza chiave)

### FASE 2 — Prima feature: Finanze (1 WP — valida l'intera architettura)

**WP-2.1 — `:feature:finance`**
- Tabelle: `finance_transaction` (importo in centesimi Long, valuta, categoria id, data, note, tipo entrata/uscita), `finance_category` (nome, icona, colore, budget mensile opzionale)
- Schermate: lista transazioni (filtri per mese/categoria), inserimento rapido, gestione categorie, schermata report con grafici Vico (spese per categoria — donut/bar; andamento entrate/uscite — linee per mese)
- Card dashboard: `finance.monthly_summary` (entrate/uscite/saldo mese corrente + mini sparkline), `finance.budget_alert` (visibile solo se un budget è oltre l'80%)
- Categorie predefinite al primo avvio (seed)
- **DoD**: CRUD completo testato, grafici corretti su dataset di test, card registrate e funzionanti in dashboard, `docs/modules/finance.md`
- **Nota per l'agente**: questo WP è il template di riferimento. Qualità e struttura verranno copiate dagli altri agenti feature: massima cura.

### FASE 3 — Feature parallele (3 WP paralleli, partono dopo review della Fase 2)

**WP-3.1 [P] — `:feature:diet`**
- Tabelle: `diet_meal` (timestamp, tipo pasto, alimenti come lista, kcal e macro totali), `diet_food` (cache alimenti: nome, brand, macro/100g, barcode, source: MANUAL|OFF), `diet_water_intake` (timestamp, ml), `diet_goal` (kcal target, macro target, ml acqua target)
- Integrazione Open Food Facts via Ktor: ricerca per nome e barcode (scanner: CameraX + ML Kit barcode — unica dipendenza extra ammessa, ADR richiesto), sempre con fallback inserimento manuale e cache locale (l'app deve essere pienamente usabile offline)
- Promemoria idratazione: `BackgroundScheduler`, finestre orarie configurabili, notifica con azione rapida "+250ml"
- Card: `diet.today_summary` (kcal + anello progresso), `diet.hydration` (progresso acqua + bottone rapido)
- **DoD**: uso completo offline verificato, promemoria testati (incluso: non suonano fuori finestra), card funzionanti

**WP-3.2 [P] — `:feature:work`**
- To-do in Room: `work_task` (titolo, note, due date opzionale, priorità, stato, `calendarEventId` opzionale per link a evento)
- Calendario: lettura eventi via CalendarProvider (permessi runtime gestiti con schermata di spiegazione), vista agenda unificata task+eventi per giorno/settimana; creazione evento di sistema da un task
- Card: `work.today` (task di oggi + prossimi eventi), `work.overdue` (visibile solo se esistono task scaduti)
- **DoD**: agenda corretta con calendari multipli, permesso negato gestito con grazia (feature degradata, non crash), card funzionanti

**WP-3.3 [P] — `:feature:wearable`**
- Health Connect: onboarding permessi granulari, sync in batch via `BackgroundScheduler` (default ogni 4h + pulsante manuale) di: frequenza cardiaca, HRV, passi, sonno, esercizio
- Tabelle: `wearable_sample` (tipo, timestamp, valore, unità, sourceApp), `wearable_daily_aggregate` (giorno, tipo, min/max/avg/somma) — gli aggregati calcolati in un worker post-sync, MAI in query UI
- Post-processing stress: indice derivato da HRV (baseline personale su finestra mobile 14 giorni, deviazione → score 0–100); documentare la formula in ADR
- Schermate: dashboard salute (trend HR/HRV/passi/sonno con Vico), heatmap stress per ora×giorno, dettaglio per metrica
- Card: `wearable.today_vitals`, `wearable.stress_now`
- Gestione assenza dati: Health Connect non installato / nessuna sorgente → empty state con istruzioni
- **DoD**: sync incrementale (non riscarica tutto), aggregati corretti su dataset sintetico di test, heatmap performante con 90 giorni di dati, zero letture Health Connect fuori dal worker

### FASE 4 — Dashboard completa (1 WP)

**WP-4.1 — Dashboard interattiva** (`:app` + `:core:dashboard-api` se servono estensioni → via Architect)
- Riordino card con drag&drop, mostra/nascondi card, persistenza in DataStore
- Layout responsive (grid adattiva), pull-to-refresh che delega alle feature
- Baseline profile + macrobenchmark su startup e scroll dashboard
- **DoD**: riordino persistente dopo riavvio, benchmark in CI con soglie (startup, jank)

### FASE 5 — Blocco biometrico e rifiniture privacy (1 WP)

**WP-5.1 — Sicurezza**
- Blocco app opzionale: BiometricPrompt all'avvio/resume (timeout configurabile)
- Schermata privacy nelle impostazioni: cosa è salvato, dove, come cancellare tutto (wipe con conferma)
- FLAG_SECURE opzionale sulle schermate finanze/salute
- **DoD**: blocco verificato su process death, wipe completo verificato

### FASE 6 — Backup cloud (1 WP)

**WP-6.1 — `:core:sync` + Google Drive**
- Snapshot: chiusura connessioni → copia file DB cifrato + settings JSON → pacchetto unico versionato (`lifemanager-backup-v<schemaVersion>-<timestamp>.lmbak`)
- Upload in Drive `appDataFolder` (Google Sign-In minimale, scope solo appDataFolder), retention ultimi N snapshot
- Pianificazione: WorkManager giornaliero con constraint WiFi+carica, più azione manuale
- Restore: lista snapshot, download, verifica integrità (checksum), import
- **DoD**: round-trip completo upload→wipe→restore su device di test, upload mai in chiaro (verificato), fallimenti rete gestiti con retry/backoff

### FASE 7 — Desktop read-only (1 WP, apre il target JVM)

**WP-7.1 — App desktop**
- Attivazione target `desktop` in tutti i moduli common; le implementazioni Android-only (Health Connect, CalendarProvider, WorkManager) sostituite da stub/no-op via expect-actual o interfacce Koin
- Apertura di uno snapshot `.lmbak` (locale o scaricato da Drive) in sola lettura: dashboard e schermate di consultazione, editing disabilitato a livello di repository (flag read-only)
- **DoD**: apertura snapshot reale prodotto da Android, tutte le schermate di consultazione funzionanti su Windows/Mac/Linux, nessuna scrittura possibile

---

## 6. Assegnazione agenti e sequenza

| Agente | WP | Dipende da |
|---|---|---|
| Architect | WP-0.1, review di ogni fase, WP-4.1 | — |
| UI | WP-1.1 | WP-0.1 |
| Data | WP-1.2 | WP-0.1 |
| Feature-Finance | WP-2.1 | WP-1.1, WP-1.2 |
| Feature-Diet | WP-3.1 | review WP-2.1 |
| Feature-Work | WP-3.2 | review WP-2.1 |
| Feature-Wearable | WP-3.3 | review WP-2.1 |
| Security | WP-5.1 | Fase 4 |
| Sync | WP-6.1 | WP-1.2, Fase 5 |
| Desktop | WP-7.1 | Fase 6 |

Punti di review obbligatori (Architect): fine Fase 0, fine Fase 2 (il template!), fine Fase 3, fine Fase 6.

## 7. Rischi noti e mitigazioni

| Rischio | Mitigazione |
|---|---|
| Vico non copre heatmap/multiplatform | Fallback Koala Plot o Canvas custom in designsystem; decidere con ADR in WP-3.3 |
| Health Connect senza HRV dalla sorgente dell'utente | Stress score degradato con messaggio chiaro; feature flag |
| Room KMP + SQLCipher: attriti d'integrazione | Spike tecnico all'inizio di WP-1.2; fallback SQLDelight (decisione Architect via ADR) |
| Drive API quota/scope review | Scope solo appDataFolder (verifica leggera); documentare setup console in docs |
| Drift di stile tra agenti feature | WP-2.1 come golden template + review Architect + Konsist/lint custom |
| Conflitti di merge su file condivisi | Punti di estensione dedicati (registry, migrazioni); un file per feature |

## 8. Backlog esplicito (fuori scope, non implementare)

- Sync bidirezionale multi-device (Livello 2) — schema già predisposto (§3.3)
- Samsung Health SDK nativo per stress score proprietario
- Target iOS / Web WASM
- Widget home screen Android, Wear OS companion
- Import estratti conto bancari (CSV/OFX)
