package com.lifemanager.database

import androidx.room.TypeConverter
import kotlin.time.Instant

/**
 * Columns always store epoch millis UTC as `Long` (§3.3); this only bridges DAO method
 * signatures that want to work with [Instant] instead of raw `Long`. Register on the
 * `@Database`/`@Dao` that needs it via `@TypeConverters(InstantConverters::class)`.
 */
class InstantConverters {
    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}
