package com.lifemanager.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertContentEquals

@RunWith(AndroidJUnit4::class)
class DatabasePassphraseManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun passphraseIsStableAcrossInstances() {
        val first = DatabasePassphraseManager(context).getOrCreatePassphrase()
        val second = DatabasePassphraseManager(context).getOrCreatePassphrase()

        assertContentEquals(first, second)
    }
}
