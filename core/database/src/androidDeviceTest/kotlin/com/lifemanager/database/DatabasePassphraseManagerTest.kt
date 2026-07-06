package com.lifemanager.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class DatabasePassphraseManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        context.deleteSharedPreferences(DatabasePassphraseManager.PREFS_FILE_NAME)
    }

    @Test
    fun passphraseIsStableAcrossInstances() {
        val first = DatabasePassphraseManager(context).getOrCreatePassphrase()
        val second = DatabasePassphraseManager(context).getOrCreatePassphrase()

        assertContentEquals(first, second)
    }

    /**
     * REVIEW-WP-1.2 P1: "manca un test che dimostri che la passphrase appena generata e'
     * persistita in modo affidabile prima dell'uso". Starts from a clean slate and checks a
     * brand-new manager instance sees the same passphrase immediately after generation, with
     * no delay — only true because [DatabasePassphraseManager.getOrCreatePassphrase] now
     * `commit()`s (blocking) instead of `apply()`ing (async) before returning.
     */
    @Test
    fun freshManagerInstanceSeesPassphraseImmediatelyAfterGeneration() {
        context.deleteSharedPreferences(DatabasePassphraseManager.PREFS_FILE_NAME)

        val generated = DatabasePassphraseManager(context).getOrCreatePassphrase()
        val readBack = DatabasePassphraseManager(context).getOrCreatePassphrase()

        assertContentEquals(generated, readBack)
    }

    /**
     * REVIEW-WP-1.2 P1: concurrent first-access race. Before the fix, two callers could both
     * read `null`, generate different passphrases, and race on which one gets persisted last —
     * leaving a DB created with the "losing" passphrase permanently unreadable. All racing
     * callers must observe the exact same passphrase.
     */
    @Test
    fun concurrentFirstAccessAcrossThreadsReturnsTheSamePassphrase() {
        context.deleteSharedPreferences(DatabasePassphraseManager.PREFS_FILE_NAME)

        val threadCount = 16
        val ready = CountDownLatch(threadCount)
        val start = CountDownLatch(1)
        val results = ConcurrentLinkedQueue<ByteArray>()

        val threads = List(threadCount) {
            Thread {
                val manager = DatabasePassphraseManager(context)
                ready.countDown()
                start.await()
                results += manager.getOrCreatePassphrase()
            }
        }
        threads.forEach { it.start() }
        ready.await()
        start.countDown()
        threads.forEach { it.join() }

        assertEquals(threadCount, results.size)
        val expected = results.first()
        results.forEach { assertContentEquals(expected, it) }
    }
}
