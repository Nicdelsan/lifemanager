import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    android {
        namespace = "com.lifemanager.dashboardapi"
        compileSdk = 37
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
        }
    }
}
