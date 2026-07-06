package com.lifemanager.database

import androidx.room.migration.Migration

/**
 * Extension point for feature migrations (implementation-plan.md §5, WP-1.2): a feature
 * module implements this and binds it in its own Koin module (e.g. `single { FinanceMigrations() }
 * bind FeatureMigrationProvider::class`). Whoever assembles the concrete `@Database` — the
 * only place that can see every feature's entities, since `core:*` never depends on
 * `feature:*` (§3.1) — collects every bound provider (`getKoin().getAll<FeatureMigrationProvider>()`)
 * and passes the flattened list to [buildEncryptedRoomDatabase]. No feature ever edits a
 * shared file to register its migrations, only its own module.
 */
fun interface FeatureMigrationProvider {
    fun migrations(): List<Migration>
}
