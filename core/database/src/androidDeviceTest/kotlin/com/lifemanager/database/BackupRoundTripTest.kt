package com.lifemanager.database

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * DoD WP-1.2: "test di round-trip export→import". Simulates the real flow: export closes and
 * copies the live DB out, the local file is then wiped (device migration / fresh install),
 * import restores it under the same canonical filename the app always uses.
 */
@RunWith(AndroidJUnit4::class)
class BackupRoundTripTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val backupManager = DatabaseBackupManager(context)
    private val passphrase = "correct-horse-battery-staple".toByteArray()

    private val dbFile = context.getDatabasePath("roundtrip.db")
    private val settingsFile = File(context.filesDir, "datastore/roundtrip.preferences_pb")
    private val exportDir = File(context.cacheDir, "roundtrip-export").apply { mkdirs() }

    private val themeKey = stringPreferencesKey("theme")
    private val hydrationTargetKey = intPreferencesKey("hydration_target_ml")

    @After
    fun tearDown() {
        dbFile.delete()
        settingsFile.delete()
        exportDir.deleteRecursively()
    }

    @Test
    fun exportThenImportRestoresDatabaseAndSettings() = runTest {
        val settings = PreferenceDataStoreFactory.create(produceFile = { settingsFile })
        settings.edit { prefs ->
            prefs[themeKey] = "dark"
            prefs[hydrationTargetKey] = 2000
        }

        val database = buildEncryptedRoomDatabase(context, TestDatabaseV1::class, dbFile.name, passphrase)
        database.noteDao().insert(NoteEntityV1("1", 0L, 0L, null, "backed up note"))

        backupManager.export(
            database = database, // export() closes it
            databaseFile = dbFile,
            settings = settings,
            destination = DocumentFile.fromFile(exportDir),
        )

        check(dbFile.delete()) { "expected the exported DB file to still be on disk before wiping it" }

        backupManager.import(
            source = DocumentFile.fromFile(exportDir),
            destinationDatabaseFile = dbFile,
            settings = settings,
        )

        val restored = buildEncryptedRoomDatabase(context, TestDatabaseV1::class, dbFile.name, passphrase)
        val notes = restored.noteDao().getAll()
        restored.close()

        assertEquals(1, notes.size)
        assertEquals("backed up note", notes.single().title)

        val restoredPrefs = settings.data.first()
        assertEquals("dark", restoredPrefs[themeKey])
        assertEquals(2000, restoredPrefs[hydrationTargetKey])
    }
}
