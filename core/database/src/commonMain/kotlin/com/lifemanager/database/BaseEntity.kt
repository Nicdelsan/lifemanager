package com.lifemanager.database

import androidx.room.PrimaryKey

/**
 * Fields every table must declare per implementation-plan.md §3.3, kept here so
 * feature entities inherit them instead of retyping the convention. `id` is a UUID v4
 * string (never autoincrement) to keep bidirectional sync (Level 2, out of scope for
 * now) migration-free later; `deletedAt` backs soft delete, read queries filter it
 * `IS NULL` at the DAO level.
 *
 * Kotlin does not carry annotations across `override`, so `@PrimaryKey` here is
 * documentation only — every concrete `@Entity` MUST re-declare
 * `@PrimaryKey override val id: String` itself, otherwise Room's annotation
 * processor rejects the entity with "must have at least 1 property annotated with
 * @PrimaryKey" (found empirically in this WP's own test entities).
 */
abstract class BaseEntity(
    @PrimaryKey
    open val id: String,
    open val createdAt: Long,
    open val updatedAt: Long,
    open val deletedAt: Long?,
)
