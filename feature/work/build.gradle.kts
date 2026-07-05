import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Skeleton only. Delivered by WP-3.2.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.lifemanager.work"
        compileSdk = 37
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvm("desktop")
}
