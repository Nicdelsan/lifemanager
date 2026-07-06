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

    fun getOrCreatePassphrase(): ByteArray {
        preferences.getString(KEY_PASSPHRASE, null)?.let { encoded ->
            return Base64.decode(encoded, Base64.NO_WRAP)
        }
        val generated = ByteArray(PASSPHRASE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        preferences.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(generated, Base64.NO_WRAP))
            .apply()
        return generated
    }

    private companion object {
        const val PREFS_FILE_NAME = "lifemanager_db_passphrase"
        const val KEY_PASSPHRASE = "passphrase"
        const val PASSPHRASE_LENGTH_BYTES = 32
    }
}
