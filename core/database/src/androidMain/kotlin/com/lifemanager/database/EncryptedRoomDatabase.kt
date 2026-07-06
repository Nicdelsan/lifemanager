package com.lifemanager.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import kotlin.reflect.KClass

/**
 * Unlike the deprecated `net.zetetic:android-database-sqlcipher`, this artifact does not
 * load its native library automatically — found empirically as an `UnsatisfiedLinkError` on
 * `nativeOpen` on a real device (WP-1.2). `by lazy` makes the one-time `System.loadLibrary`
 * call thread-safe without reloading on every database open.
 */
private val sqlCipherNativeLibraryLoaded: Unit by lazy { System.loadLibrary("sqlcipher") }

/**
 * Shared encrypted-builder ("builder centrale", implementation-plan.md §5 WP-1.2) that
 * every feature's concrete `@Database` goes through. It cannot itself hold `entities = [...]`
 * for every feature — Room needs that list at the compile site of the `@Database` class, and
 * `core:*` never depends on `feature:*` (§3.1) — so the concrete class is assembled wherever
 * every feature is visible (`:app`) and passed in here only as a [KClass].
 */
fun <T : RoomDatabase> buildEncryptedRoomDatabase(
    context: Context,
    klass: KClass<T>,
    databaseName: String,
    passphrase: ByteArray,
    migrations: List<Migration> = emptyList(),
): T {
    sqlCipherNativeLibraryLoaded
    val builder = Room.databaseBuilder(context.applicationContext, klass.java, databaseName)
        .openHelperFactory(SupportOpenHelperFactory(passphrase))
    if (migrations.isNotEmpty()) {
        builder.addMigrations(*migrations.toTypedArray())
    }
    return builder.build()
}
