import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Skeleton only. Backup/export/import + Google Drive appDataFolder sync are
// delivered by WP-6.1.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.lifemanager.sync"
        compileSdk = 37
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvm("desktop")
}
