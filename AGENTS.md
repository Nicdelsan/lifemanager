# LifeManager — Istruzioni per l'agente

1. Leggi PRIMA di tutto `docs/implementation-plan.md`. Le sezioni 1–4 sono
   VINCOLANTI e non modificabili (solo l'Agente Architect può cambiarle,
   e solo su richiesta esplicita dell'utente).
2. Il tuo incarico corrente è definito in `docs/current-wp.md`. Lavora SOLO
   sui moduli elencati lì. Uniche eccezioni ammesse: registrare il proprio
   FeatureModule nel registry in `:app` e aggiungere le proprie migrazioni
   nel punto di estensione dedicato in `:core:database`.
3. Se un contratto condiviso (interfacce in :core:dashboard-api, convenzioni
   DB §3.3, version catalog) non basta per il tuo compito: NON modificarlo.
   Scrivi una proposta in `docs/decisions/PROPOSAL-<n>.md` con problema,
   opzioni e raccomandazione, poi fermati su quel punto e prosegui con il
   resto del WP se possibile.
4. Prima di dichiarare concluso il lavoro: `./gradlew build` e
   `./gradlew test` devono essere verdi, la documentazione in
   `docs/modules/<modulo>.md` aggiornata, e la Definition of Done del WP
   soddisfatta punto per punto (verificala esplicitamente, una voce alla volta).
5. Commit: Conventional Commits (es. `feat(finance): add transaction list`).
   Lavora sul branch indicato in `docs/current-wp.md`. MAI committare
   direttamente su main. MAI fare push senza conferma dell'utente.
6. Nuove dipendenze: solo se strettamente necessarie, con ADR in
   `docs/decisions/ADR-<n>.md` (contesto, opzioni, scelta, conseguenze).
   Le versioni si dichiarano SOLO in `gradle/libs.versions.toml`.
7. Funzionalità rimandate o fuori scope: annotale in `docs/backlog.md`,
   mai come TODO silenziosi nel codice.
8. Lingua del codice: inglese (nomi, commenti). Lingua delle stringhe
   utente: italiano (via risorse, mai hardcoded).
