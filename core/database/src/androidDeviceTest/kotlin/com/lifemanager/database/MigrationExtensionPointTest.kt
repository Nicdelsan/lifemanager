package com.lifemanager.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises [FeatureMigrationProvider] end to end through [buildEncryptedRoomDatabase]: a
 * feature registers its migration, whoever assembles the concrete `@Database` (`:app`, per
 * ADR-003) passes the flattened list through unchanged.
 */
@RunWith(AndroidJUnit4::class)
class MigrationExtensionPointTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbFile = context.getDatabasePath("migration-test.db")
    private val passphrase = "correct-horse-battery-staple".toByteArray()

    @After
    fun tearDown() {
        dbFile.delete()
    }

    @Test
    fun featureMigrationProviderMigrationsAreAppliedOnUpgrade() = runTest {
        val v1 = buildEncryptedRoomDatabase(context, TestDatabaseV1::class, dbFile.name, passphrase)
        v1.noteDao().insert(NoteEntityV1("1", 0L, 0L, null, "pre-migration note"))
        v1.close()

        val featureMigrations = TestFeatureMigrations().migrations()
        val v2 = buildEncryptedRoomDatabase(
            context,
            TestDatabaseV2::class,
            dbFile.name,
            passphrase,
            migrations = featureMigrations,
        )
        val notes = v2.noteDao().getAll()
        v2.close()

        assertEquals(1, notes.size)
        assertEquals("pre-migration note", notes.single().title)
        assertTrue(featureMigrations.isNotEmpty())
    }
}
