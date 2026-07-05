# Assegnazione Work Package → Strumento

| WP | Strumento | Motivo |
|---|---|---|
| WP-0.1 Bootstrap | Claude Code | Massima criticità architetturale |
| WP-1.1 Design system | Codex | Spec chiusa, parallelo a WP-1.2 |
| WP-1.2 Database core | Claude Code | Room KMP + SQLCipher = punto tecnico più rischioso |
| WP-2.1 Finance (golden template) | Claude Code | Verrà copiato da tutti |
| WP-3.1 Diet | Antigravity 2.0 (worktree) | Parallelismo Fase 3 |
| WP-3.2 Work | Antigravity 2.0 (worktree) | Parallelismo Fase 3 |
| WP-3.3 Wearable | Claude Code | Il più incerto (Health Connect, aggregati, stress) |
| WP-4.1 Dashboard | Claude Code | Tocca i contratti condivisi |
| WP-5.1 Sicurezza | Codex | Ben delimitato |
| WP-6.1 Sync Drive | Claude Code | Cifratura e integrità dati |
| WP-7.1 Desktop | Antigravity o Claude Code | UI su architettura rodata |
| Review di ogni WP | Codex | Cross-model review (modello diverso da chi ha scritto) |

Punti di review umana obbligatori: fine Fase 0, fine Fase 2, fine Fase 3, fine Fase 6.

## Flusso standard di un WP
1. Aggiornare docs/current-wp.md (WP, strumento, branch, moduli)
2. Creare il branch `wp/<n>-<nome>` (o worktree per la Fase 3)
3. Lanciare l'agente assegnato (comando start-wp / prompt implement-wp)
4. A lavoro finito: PR verso main → CI verde
5. Review Codex con docs/prompts/review-wp.md → report in docs/decisions/REVIEW-<wp>.md
6. Correzioni eventuali → merge → aggiornare current-wp.md al WP successivo
