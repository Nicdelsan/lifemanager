import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        namespace = "com.lifemanager.database"
        compileSdk = 37
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        // SQLCipher ships Android-only native libraries: they cannot load under a JVM
        // host-test process (Robolectric), so encryption/round-trip verification must run
        // as an instrumented test on a real device/emulator instead of a host test.
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // AGP's default (page-aligned, uncompressed) native lib packaging makes
        // SQLCipher's libsqlcipher.so fail to load ("UnsatisfiedLinkError: ... nativeOpen"),
        // found empirically running the encryption tests on a real device (WP-1.2).
        packaging {
            jniLibs.useLegacyPackaging = true
        }
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.sqlcipher.android)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.security.crypto)
            implementation(libs.androidx.documentfile)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.serialization.json)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.room.runtime)
                implementation(libs.sqlcipher.android)
                implementation(libs.androidx.sqlite)
                implementation(libs.androidx.security.crypto)
                implementation(libs.androidx.documentfile)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.androidx.test.runner)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    add("kspAndroidDeviceTest", libs.room.compiler)
}
