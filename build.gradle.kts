import org.gradle.api.artifacts.ProjectDependency

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

// Enforces the section 3.1 module dependency rules on the *actual* Gradle project
// graph (feature:* never -> another feature:*, core:* never -> feature:*).
// Konsist (:konsist-tests) only sees Kotlin source imports, so a forbidden
// Gradle dependency with no matching import yet would slip past it; this
// check runs at configuration time for every Gradle invocation instead.
allprojects {
    afterEvaluate {
        val modulePath = path
        val isFeature = modulePath.startsWith(":feature:")
        val isCore = modulePath.startsWith(":core:")
        if (!isFeature && !isCore) return@afterEvaluate

        configurations.forEach { configuration ->
            configuration.dependencies
                .filterIsInstance<ProjectDependency>()
                .forEach { dependency ->
                    val dependencyPath = dependency.path
                    if (isFeature && dependencyPath.startsWith(":feature:") && dependencyPath != modulePath) {
                        throw GradleException(
                            "Module dependency rule violation: $modulePath -> $dependencyPath. " +
                                "A feature module must never depend on another feature module " +
                                "(implementation-plan.md section 3.1).",
                        )
                    }
                    if (isCore && dependencyPath.startsWith(":feature:")) {
                        throw GradleException(
                            "Module dependency rule violation: $modulePath -> $dependencyPath. " +
                                "A core module must never depend on a feature module " +
                                "(implementation-plan.md section 3.1).",
                        )
                    }
                }
        }
    }
}
