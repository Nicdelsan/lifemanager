package com.lifemanager.database

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Generates and stores the SQLCipher passphrase. The passphrase itself sits in
 * [EncryptedSharedPreferences], whose value is AES-256-GCM-encrypted with a key held in the
 * Android Keystore (via [MasterKey]) and never leaves the device — this is the officially
 * recommended wrapper over hand-rolled `KeyStore`/`Cipher` calls, which is why we use it here
 * instead of talking to `java.security.KeyStore` directly.
 */
class DatabasePassphraseManager(context: Context) {

    private val preferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_FILE_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    /**
     * Read-generate-write is synchronized on [LOCK] (class-wide, not per-instance) because two
     * concurrent first-access callers — each with their own [DatabasePassphraseManager] — could
     * otherwise both read `null`, generate different passphrases, and race on which one gets
     * persisted last; a DB opened with the "losing" passphrase becomes permanently unreadable.
     * The write itself uses [android.content.SharedPreferences.Editor.commit] (blocking) instead
     * of `apply()` so the passphrase is durably on disk before any caller can use it to open a
     * database — `apply()` schedules the disk write asynchronously and returns immediately.
     */
    fun getOrCreatePassphrase(): ByteArray = synchronized(LOCK) {
        preferences.getString(KEY_PASSPHRASE, null)?.let { encoded ->
            return@synchronized Base64.decode(encoded, Base64.NO_WRAP)
        }
        val generated = ByteArray(PASSPHRASE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val persisted = preferences.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(generated, Base64.NO_WRAP))
            .commit()
        check(persisted) { "Failed to persist the SQLCipher passphrase" }
        generated
    }

    companion object {
        // Public so tests can reset state via `context.deleteSharedPreferences(...)`; the file
        // name itself is not sensitive.
        const val PREFS_FILE_NAME = "lifemanager_db_passphrase"
        private const val KEY_PASSPHRASE = "passphrase"
        private const val PASSPHRASE_LENGTH_BYTES = 32
        private val LOCK = Any()
    }
}
