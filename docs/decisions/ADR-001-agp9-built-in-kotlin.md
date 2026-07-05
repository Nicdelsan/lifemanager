# ADR-001 — Build Android/KMP con AGP 9 built-in Kotlin

- Stato: accettato
- Data: 2026-07-05
- WP: WP-0.1
- Autore: Claude Code (Agente Architect)

## Contesto
Al momento del bootstrap, la versione stabile più recente di Android Gradle
Plugin è la 9.2.1. AGP 9 introduce il "built-in Kotlin support": il plugin
`org.jetbrains.kotlin.android` non è più applicabile a un modulo che usa
`com.android.application`/`com.android.library`, e la combinazione classica
`com.android.library` + `org.jetbrains.kotlin.multiplatform` per i moduli KMP
è stata sostituita da un plugin dedicato.

Questo non era noto a priori (è successivo alla data di training del modello)
ed è stato scoperto empiricamente eseguendo `./gradlew build` e leggendo gli
errori di Gradle.

## Opzioni considerate
1. **Opt-out** (`android.builtInKotlin=false`, `android.newDsl=false`) per
   restare sul comportamento AGP 8.x — pro: build file "familiari"; contro:
   deprecato, la possibilità di opt-out sarà rimossa in AGP 10 (metà 2026),
   quindi è un debito tecnico immediato.
2. **Adottare il nuovo modello nativamente** — pro: allineato al futuro di
   AGP, nessun debito da ripagare; contro: DSL diverso da quello noto,
   richiede il nuovo plugin `com.android.kotlin.multiplatform.library` per i
   moduli KMP.

## Decisione
Adottato il nuovo modello (opzione 2):
- `:app` (Android puro, non KMP): solo `com.android.application` +
  `org.jetbrains.kotlin.plugin.compose` (compose compiler). Niente
  `org.jetbrains.kotlin.android`. Compose UI tramite BOM
  (`androidx.compose:compose-bom`) e artefatti `androidx.compose.*` diretti,
  non tramite il plugin Compose Multiplatform (non necessario per un modulo
  solo-Android).
- `core:*` e `feature:*` (moduli KMP con target `android` + `desktop`
  dichiarato): `org.jetbrains.kotlin.multiplatform` +
  `com.android.kotlin.multiplatform.library`. Configurazione Android
  interamente dentro `kotlin { android { ... } }`, non più in un blocco
  `android { }` separato. Test JVM/Android disabilitati di default dal nuovo
  plugin: vanno abilitati esplicitamente con `withHostTestBuilder {}` dove
  servono (fatto in `:core:common`).
- `compileSdk`/`targetSdk` impostati a 37 anziché 36: alcune dipendenze
  AndroidX correnti (`core-ktx`, `lifecycle-*`) richiedono compileSdk 37+
  (verificato da un fallimento reale di `checkDebugAarMetadata`).

## Conseguenze
- I build file di `core:*`/`feature:*` non useranno il blocco `android {}`
  di primo livello familiare ad AGP 8.x: chi imposta un nuovo modulo deve
  seguire lo schema `kotlin { android { ... }; jvm("desktop"); sourceSets {} }`
  già presente negli altri moduli come riferimento.
- Se un modulo KMP futuro necessita di varianti di build (flavor/buildType),
  il nuovo plugin non le supporta ("single variant architecture"): valutare
  se estrarre la logica android-specifica in un modulo `com.android.library`
  separato consumato da `androidMain`, come suggerito dalla documentazione
  ufficiale di migrazione.
- `local.properties` (non versionato) deve usare `:` escappato
  (`sdk.dir=C\:/percorso/...`) per non far fallire il lint `PropertyEscape`;
  in alternativa il check è disabilitato in `:app` perché il file è
  puramente locale e non esiste in CI.
