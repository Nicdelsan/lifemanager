package com.lifemanager.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * REVIEW-WP-1.2 P2: the previous `StringSet` encoding joined elements with `" "` into a single
 * string, which is lossy for elements that are empty or already contain the delimiter. These
 * tests pin the edge cases the review flagged, plus a full round-trip of every supported type.
 */
@RunWith(AndroidJUnit4::class)
class SettingsSnapshotTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // DataStore tracks "active file path" process-wide even after the backing file is deleted,
    // so reusing one fixed file name across test methods throws "multiple DataStores active
    // for the same file" on the second test. Each test gets its own file instead — the counter
    // is static because JUnit creates a fresh test instance per @Test method.
    private val storeFiles = mutableListOf<File>()

    private val stringKey = stringPreferencesKey("s")
    private val intKey = intPreferencesKey("i")
    private val longKey = longPreferencesKey("l")
    private val floatKey = floatPreferencesKey("f")
    private val booleanKey = booleanPreferencesKey("b")
    private val stringSetKey = stringSetPreferencesKey("set")

    @After
    fun tearDown() {
        storeFiles.forEach { it.delete() }
    }

    @Test
    fun roundTripPreservesEveryScalarTypeAndAStringSet() = runTest {
        val store = newStore()
        store.edit { prefs ->
            prefs[stringKey] = "hello"
            prefs[intKey] = 42
            prefs[longKey] = Long.MAX_VALUE
            prefs[floatKey] = 3.14f
            prefs[booleanKey] = true
            prefs[stringSetKey] = setOf("a", "b", "c")
        }

        val restored = roundTrip(store)

        assertEquals("hello", restored[stringKey])
        assertEquals(42, restored[intKey])
        assertEquals(Long.MAX_VALUE, restored[longKey])
        assertEquals(3.14f, restored[floatKey])
        assertEquals(true, restored[booleanKey])
        assertEquals(setOf("a", "b", "c"), restored[stringSetKey])
    }

    @Test
    fun stringSetWithEmptyStringElementSurvivesRoundTrip() = runTest {
        val store = newStore()
        store.edit { prefs -> prefs[stringSetKey] = setOf("") }

        val restored = roundTrip(store)

        assertEquals(setOf(""), restored[stringSetKey])
    }

    @Test
    fun stringSetElementContainingTheOldDelimiterSurvivesRoundTrip() = runTest {
        val store = newStore()
        store.edit { prefs -> prefs[stringSetKey] = setOf("a b", "c d") }

        val restored = roundTrip(store)

        assertEquals(setOf("a b", "c d"), restored[stringSetKey])
    }

    @Test
    fun emptyStringSetSurvivesRoundTrip() = runTest {
        val store = newStore()
        store.edit { prefs -> prefs[stringSetKey] = emptySet() }

        val restored = roundTrip(store)

        assertEquals(emptySet(), restored[stringSetKey])
    }

    private fun newStore(): DataStore<Preferences> {
        val file = File(context.filesDir, "datastore/settings_snapshot_test_${fileCounter.incrementAndGet()}.preferences_pb")
        file.delete()
        storeFiles += file
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }

    private suspend fun roundTrip(store: DataStore<Preferences>): Preferences {
        val snapshot = store.data.first().toSettingsSnapshot()
        store.restoreFromSnapshot(snapshot)
        return store.data.first()
    }

    private companion object {
        val fileCounter = AtomicInteger()
    }
}
