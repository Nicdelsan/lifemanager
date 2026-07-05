import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Skeleton only. Room + SQLCipher wiring, BaseEntity conventions and the
// feature-migration extension point are delivered by WP-1.2.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.lifemanager.database"
        compileSdk = 37
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvm("desktop")
}
