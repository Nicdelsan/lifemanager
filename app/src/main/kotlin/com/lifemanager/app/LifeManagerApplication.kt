package com.lifemanager.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LifeManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LifeManagerApplication)
            modules(FeatureModuleRegistry.modules.map { it.koinModule })
        }
    }
}
