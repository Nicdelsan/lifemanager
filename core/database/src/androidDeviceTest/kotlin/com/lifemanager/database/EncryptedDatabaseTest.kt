package com.lifemanager.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * DoD WP-1.2: "test cifratura (il file non è leggibile senza chiave)".
 */
@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbFile = context.getDatabasePath("encryption-test.db")

    @After
    fun tearDown() {
        dbFile.delete()
    }

    @Test
    fun fileIsUnreadableWithoutThePassphrase() = runTest {
        val passphrase = "correct-horse-battery-staple".toByteArray()
        val db = buildEncryptedRoomDatabase(context, TestDatabaseV1::class, dbFile.name, passphrase)
        db.noteDao().insert(NoteEntityV1("1", 0L, 0L, null, "hello"))
        db.close()

        assertTrue(dbFile.exists())
        assertFailsWith<SQLiteException> {
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        }
    }

    @Test
    fun fileIsReadableWithTheCorrectPassphrase() = runTest {
        val passphrase = "correct-horse-battery-staple".toByteArray()
        val db = buildEncryptedRoomDatabase(context, TestDatabaseV1::class, dbFile.name, passphrase)
        db.noteDao().insert(NoteEntityV1("1", 0L, 0L, null, "hello"))
        db.close()

        val reopened = buildEncryptedRoomDatabase(context, TestDatabaseV1::class, dbFile.name, passphrase)
        val notes = reopened.noteDao().getAll()
        reopened.close()

        assertEquals(1, notes.size)
        assertEquals("hello", notes.single().title)
    }
}
