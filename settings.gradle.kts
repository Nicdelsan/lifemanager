pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "LifeManager"

include(
    ":app",
    ":core:designsystem",
    ":core:database",
    ":core:common",
    ":core:dashboard-api",
    ":core:sync",
    ":feature:finance",
    ":feature:diet",
    ":feature:work",
    ":feature:wearable",
    ":konsist-tests",
)
