# WP attivo: WP-0.1 — Bootstrap del progetto

- Strumento assegnato: Claude Code (ruolo: Agente Architect)
- Branch: wp/0.1-bootstrap
- Moduli modificabili: TUTTI (questo WP crea lo scheletro del progetto)
- Riferimento Definition of Done: docs/implementation-plan.md → FASE 0

## Note di contesto
- Il progetto parte da questa cartella: esistono solo docs/, CLAUDE.md,
  AGENTS.md e la CI. Tutto il codice va creato da zero seguendo §3.1.
- Target KMP: android attivo; desktop (JVM) dichiarato ma senza sorgenti.
- La CI in .github/workflows/ci.yml è una bozza: completala con il job
  Konsist quando i test di architettura esistono.
- Dispositivo di riferimento dell'utente: Samsung Galaxy S25 Ultra
  (test via adb) + Galaxy Watch 7 (rilevante solo dalla Fase 3).
- Al termine: apri una PR verso main, non fare merge.
