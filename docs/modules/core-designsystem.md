# :core:designsystem

Modulo Kotlin Multiplatform che espone il tema e i componenti UI riusabili di
LifeManager. Il codice vive in `commonMain` per essere condiviso tra Android e
il futuro target desktop.

## Tema

`LifeManagerTheme` applica Material 3 con:
- palette Notion-style neutra, con superfici piatte, bordi sottili e accenti
  blu/verde/ambra per evitare una UI monocromatica;
- dark mode grigio-carbone tramite `isSystemInDarkTheme()`;
- tipografia sans-serif con gerarchia marcata;
- shape moderatamente arrotondate, con card a 8dp.

## Componenti

API pubbliche:
- `LmCard`: superficie piatta con bordo sottile, contenuto a colonna e click
  opzionale.
- `LmSectionHeader`: header sezione con emoji o `ImageVector` e azione
  opzionale.
- `LmListItem`: riga lista densa con contenuto leading/trailing, support text,
  stato disabled e divider opzionale.
- `LmEmptyState`: stato vuoto con azione primaria opzionale.
- `LmErrorState`: stato errore recuperabile con azione opzionale.
- `LmLoadingState`: stato caricamento non bloccante.
- `LmStatTile`: tile statistico con tono `Neutral`, `Positive`, `Warning` o
  `Critical`.
- `LmTopBar`: top bar Material 3 con titolo, sottotitolo, navigation icon e
  actions.

I testi sono parametri dei componenti: le feature devono passarli da risorse,
come richiesto dalle convenzioni di progetto.

## Catalogo

`LmComponentCatalogScreen` e' un catalogo navigabile in Compose con sezioni per
tema, card, liste, stati e top bar. Il modulo espone anche preview Compose per
ogni componente e preview light/dark del catalogo.

Eccezione di perimetro approvata dall'utente: `:app` monta il catalogo solo nel
source set `debug` sulla route `debug/design-system-catalog`. Nel source set
`release` la stessa estensione di navigazione e' una no-op, quindi il catalogo
non viene registrato nelle build release. La route debug espone anche il deep
link `lifemanager://debug/design-system-catalog` per aprire il catalogo senza
aggiungere UI di navigazione alla dashboard.
