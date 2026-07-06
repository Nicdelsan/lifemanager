package com.lifemanager.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.documentfile.provider.DocumentFile
import androidx.room.RoomDatabase
import java.io.File
import java.io.OutputStream
import kotlinx.serialization.json.Json

/**
 * Local export/import of the encrypted DB file plus a JSON snapshot of app settings
 * (implementation-plan.md §5 WP-1.2). Callers resolve the destination/source folder to a
 * [DocumentFile] themselves — production code via `DocumentFile.fromTreeUri` after a SAF
 * "open document tree" picker (e.g. `Documents/`, no storage permission needed), tests via
 * `DocumentFile.fromFile` on a plain temp directory.
 */
class DatabaseBackupManager(private val context: Context) {

    private val json = Json { prettyPrint = true }

    /** Closes [database] before copying, per the DoD ("funzione che chiude il DB, copia..."). */
    suspend fun export(
        database: RoomDatabase,
        databaseFile: File,
        settings: DataStore<Preferences>,
        destination: DocumentFile,
    ) {
        database.close()
        val settingsJson = json.encodeToString(SettingsSnapshot.serializer(), settings.currentSnapshot())

        writeFile(destination, databaseFile.name, GENERIC_MIME_TYPE) { output ->
            databaseFile.inputStream().use { it.copyTo(output) }
        }
        writeFile(destination, SETTINGS_FILE_NAME, GENERIC_MIME_TYPE) { output ->
            output.write(settingsJson.toByteArray())
        }
    }

    /** Closes [existingDatabase] (if the file being overwritten is currently open) before copying. */
    suspend fun import(
        source: DocumentFile,
        destinationDatabaseFile: File,
        settings: DataStore<Preferences>,
        existingDatabase: RoomDatabase? = null,
    ) {
        val dbDocument = source.findFile(destinationDatabaseFile.name)
            ?: error("Missing ${destinationDatabaseFile.name} in backup")
        val settingsDocument = source.findFile(SETTINGS_FILE_NAME)
            ?: error("Missing $SETTINGS_FILE_NAME in backup")

        existingDatabase?.close()

        context.contentResolver.openInputStream(dbDocument.uri)?.use { input ->
            destinationDatabaseFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Cannot read ${destinationDatabaseFile.name} from backup")

        val settingsBytes = context.contentResolver.openInputStream(settingsDocument.uri)?.use { it.readBytes() }
            ?: error("Cannot read $SETTINGS_FILE_NAME from backup")
        val snapshot = json.decodeFromString(SettingsSnapshot.serializer(), settingsBytes.decodeToString())
        settings.restoreFromSnapshot(snapshot)
    }

    private fun writeFile(directory: DocumentFile, name: String, mimeType: String, write: (OutputStream) -> Unit) {
        directory.findFile(name)?.delete()
        val target = directory.createFile(mimeType, name) ?: error("Cannot create $name in destination folder")
        context.contentResolver.openOutputStream(target.uri)?.use(write) ?: error("Cannot open $name for writing")
    }

    private companion object {
        const val SETTINGS_FILE_NAME = "lifemanager-settings.json"

        // DocumentFile.createFile() appends the extension MimeTypeMap associates with the
        // given MIME type to the display name — unconditionally on some providers (found
        // empirically: "application/octet-stream" became "<name>.bin" on a real device),
        // which breaks the exact-name lookup import() relies on. "*/*" has no registered
        // extension anywhere, so the name we pass in is the name we get back.
        const val GENERIC_MIME_TYPE = "*/*"
    }
}
