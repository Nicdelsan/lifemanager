# :konsist-tests

Test di architettura (Konsist 0.17.3) che fanno rispettare le regole di
dipendenza tra moduli di implementation-plan.md §3.1:
- `feature:*` può dipendere solo da `core:*`, mai da un'altra feature.
- `core:*` non dipende mai da `feature:*`.
- `:app` è l'unico modulo che può dipendere da tutto.

Implementato con `Konsist.scopeFromProject().assertArchitecture { }` e un
`Layer` per ogni modulo (`ArchitectureTest.kt`). Nota d'uso: Konsist richiede
che ogni `Layer` dichiarato contenga almeno un file Kotlin nel proprio
package, altrimenti l'assert fallisce con `KoPreconditionFailedException`
— per questo anche i moduli ancora vuoti (`core:database`, `core:sync`,
tutte le `feature:*`) hanno un file placeholder minimo.

Quando una feature futura ha bisogno di una nuova dipendenza da un modulo
`core` non ancora prevista in `ArchitectureTest.kt`, non è tra le eccezioni
di perimetro ammesse (implementation-plan.md §4.1): va segnalato
all'Agente Architect, non modificato direttamente.

Eseguito in CI come step dedicato (`.github/workflows/ci.yml`) oltre che
come parte di `./gradlew test`.
