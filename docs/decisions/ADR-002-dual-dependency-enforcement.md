# ADR-002 — Doppio meccanismo per far rispettare le regole di dipendenza §3.1

- Stato: accettato
- Data: 2026-07-05
- WP: WP-0.1
- Autore: Claude Code (Agente Architect)

## Contesto
`REVIEW-WP-0.1.md` ha rilevato che il test Konsist iniziale
(`konsist-tests/src/test/kotlin/.../ArchitectureTest.kt`) verifica solo gli
import nel codice sorgente Kotlin, non il grafo reale delle dipendenze
Gradle. Una `implementation(project(":feature:x"))` dichiarata in un
`build.gradle.kts` ma non ancora usata da nessun import passerebbe
inosservata: la regola vincolante di implementation-plan.md §3.1 è però
sui *moduli Gradle*, non sugli import.

## Opzioni considerate
1. **Sostituire Konsist con un solo check Gradle** — pro: una fonte di
   verità; contro: si perde lo strumento pensato per crescere con regole
   di stile/naming più fini nei WP successivi (già presente e funzionante).
2. **Aggiungere un secondo check, complementare, sul grafo Gradle reale**
   — pro: chiude subito il gap segnalato senza buttare via Konsist; contro:
   due meccanismi da mantenere allineati concettualmente.

## Decisione
Opzione 2. Aggiunto in `build.gradle.kts` (root) un blocco
`allprojects { afterEvaluate { ... } }` che ispeziona le `configurations`
di ogni modulo `core:*`/`feature:*` e cerca `ProjectDependency` verso una
`feature:*` non consentita, lanciando `GradleException` a livello di
configurazione (quindi su qualunque invocazione Gradle, non solo
`./gradlew test`). Konsist resta per l'enforcement a livello di stile del
codice sorgente.

Verifica: aggiunta temporaneamente `feature:finance -> feature:diet`,
confermato il fallimento con messaggio esplicito, poi rimossa (nessun test
automatico permanente — vedi `docs/backlog.md`).

## Conseguenze
- Chi introduce una nuova dipendenza Gradle vietata lo scopre al primo
  `./gradlew <qualsiasi task>`, prima ancora di scrivere codice che la usi.
- Manca ancora un test automatico (TestKit) che dimostri il fallimento in
  CI in modo riproducibile — tracciato in `docs/backlog.md`.
- Se in futuro le regole di dipendenza cambiano (es. una feature autorizzata
  a dipendere da un'altra in casi specifici), vanno aggiornati **due**
  punti: `ArchitectureTest.kt` e il blocco in `build.gradle.kts` root.
