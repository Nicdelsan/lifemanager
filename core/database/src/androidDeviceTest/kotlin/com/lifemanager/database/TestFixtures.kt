package com.lifemanager.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Minimal `@Entity`/`@Dao`/`@Database` used only to exercise the shared helpers in this
 * module's own instrumented tests. Never shipped: the real concrete `@Database` combining
 * every feature's tables is assembled in `:app` (see ADR-003), since `core:database` never
 * depends on `feature:*`.
 */
@Entity(tableName = "note")
data class NoteEntityV1(
    @PrimaryKey override val id: String,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val deletedAt: Long?,
    val title: String,
) : BaseEntity(id, createdAt, updatedAt, deletedAt)

@Dao
interface NoteDaoV1 {
    @Insert
    suspend fun insert(note: NoteEntityV1)

    @Query("SELECT * FROM note WHERE deletedAt IS NULL")
    suspend fun getAll(): List<NoteEntityV1>
}

@Database(entities = [NoteEntityV1::class], version = 1, exportSchema = false)
abstract class TestDatabaseV1 : RoomDatabase() {
    abstract fun noteDao(): NoteDaoV1
}

@Entity(tableName = "note")
data class NoteEntityV2(
    @PrimaryKey override val id: String,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val deletedAt: Long?,
    val title: String,
    val pinned: Boolean,
) : BaseEntity(id, createdAt, updatedAt, deletedAt)

@Dao
interface NoteDaoV2 {
    @Insert
    suspend fun insert(note: NoteEntityV2)

    @Query("SELECT * FROM note WHERE deletedAt IS NULL")
    suspend fun getAll(): List<NoteEntityV2>
}

@Database(entities = [NoteEntityV2::class], version = 2, exportSchema = false)
abstract class TestDatabaseV2 : RoomDatabase() {
    abstract fun noteDao(): NoteDaoV2
}

val NOTE_ADD_PINNED_MIGRATION = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE note ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
    }
}

class TestFeatureMigrations : FeatureMigrationProvider {
    override fun migrations(): List<Migration> = listOf(NOTE_ADD_PINNED_MIGRATION)
}
