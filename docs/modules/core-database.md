# :core:database

**Scheletro vuoto da WP-0.1.** Solo modulo Gradle (KMP, target `android` +
`desktop` dichiarato) senza Room, senza cifratura, senza migrazioni: tutto
il contenuto reale (RoomDatabase + SQLCipher, convenzioni `BaseEntity` da
implementation-plan.md §3.3, export/import locale) è responsabilità di
WP-1.2.

Il file placeholder `Database.kt` esiste solo per soddisfare il vincolo di
Konsist "ogni layer architetturale deve contenere almeno un file" (vedi
`:konsist-tests`); WP-1.2 lo sostituisce con contenuto reale.
