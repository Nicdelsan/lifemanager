plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "com.lifemanager.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.lifemanager.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        // local.properties is machine-generated and gitignored; its exact
        // escaping is outside our control and irrelevant to CI (no such
        // file there).
        disable += "PropertyEscape"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // :app depends on every core/feature module per implementation-plan.md
    // §3.1 ("app dipende da tutto ed è l'unico punto in cui le feature
    // vengono composte"), even before a module has real content, so a
    // feature WP only ever needs its own module plus the one-line
    // FeatureModuleRegistry entry (never a change here).
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:dashboard-api"))
    implementation(project(":core:database"))
    implementation(project(":core:sync"))
    implementation(project(":feature:finance"))
    implementation(project(":feature:diet"))
    implementation(project(":feature:work"))
    implementation(project(":feature:wearable"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.koin.android)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.robolectric)
}
