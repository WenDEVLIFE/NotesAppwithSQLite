package com.example.notesappwithsqlite.databaseController

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.notesappwithsqlite.model.Folder
import com.example.notesappwithsqlite.model.Note
import java.time.LocalDateTime

class NoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notesapp.db"
        private const val DATABASE_VERSION = 4  // Updated version for migration

        // Notes Table
        private const val TABLE_NOTES = "allnotes"
        private const val COLUMN_NOTE_ID = "id"
        private const val COLUMN_FOLDER_ID = "folder_id" // Foreign Key
        private const val COLUMN_SUBJECT = "subject"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_PRIORITY = "priority"

        // Folders Table
        private const val TABLE_FOLDERS = "allfolders"
        private const val COLUMN_FOLDER_ID_PRIMARY = "id"
        private const val COLUMN_FOLDER_NAME = "name"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_DATE1 = "date" // Ensured consistency
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createFoldersTable = """
            CREATE TABLE $TABLE_FOLDERS (
                $COLUMN_FOLDER_ID_PRIMARY INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FOLDER_NAME TEXT NOT NULL,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_DATE1 TEXT NOT NULL
            )
        """.trimIndent()

        val createNotesTable = """
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FOLDER_ID INTEGER NOT NULL,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_SUBJECT TEXT,
                FOREIGN KEY($COLUMN_FOLDER_ID) REFERENCES $TABLE_FOLDERS($COLUMN_FOLDER_ID_PRIMARY) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createFoldersTable)
        db?.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_FOLDERS ADD COLUMN $COLUMN_DATE1 TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 4) {
            db?.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_PRIORITY TEXT DEFAULT 'Low'") // Add priority column
        }
    }


    /** Get All Folders */
    fun getAllFolders(): List<Folder> {
        val folderList = mutableListOf<Folder>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FOLDERS", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID_PRIMARY))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE1))
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            folderList.add(Folder(id, name, date, userId))
        }

        cursor.close()
        db.close()
        return folderList
    }

    /** Insert a Folder */
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertFolder(folderName: String, userId: Int?, input: EditText, refreshCallback: () -> Unit) : Long {
        try {
            val db = writableDatabase
            val localDate = LocalDateTime.now()
            val date = localDate.toString()

            val values = ContentValues().apply {
                put(COLUMN_FOLDER_NAME, folderName)
                put(COLUMN_USER_ID, userId)
                put(COLUMN_DATE1, date) // Fixed column reference
            }

            val folderId = db.insert(TABLE_FOLDERS, null, values)
            db.close()
            input.setText("")
            Toast.makeText(input.context, "Folder added successfully!", Toast.LENGTH_SHORT).show()
            refreshCallback()
            return folderId
        } catch (e: Exception) {
            Toast.makeText(input.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            return -1
        }
    }

    /** Update Folder */
    fun updateFolder(folder: Folder) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOLDER_NAME, folder.name)
        }
        db.update(TABLE_FOLDERS, values, "$COLUMN_FOLDER_ID_PRIMARY = ?", arrayOf(folder.id.toString()))
        db.close()
    }

    /** Update Note */
    fun updateNote(note: Note) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_PRIORITY, note.priority)
        }
        db.update(TABLE_NOTES, values, "$COLUMN_NOTE_ID = ?", arrayOf(note.id.toString()))
        db.close()
    }

    /** Insert Note */
    fun insertNote(note: Note, folderId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOLDER_ID, folderId)
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_PRIORITY, note.priority)
        }
        db.insert(TABLE_NOTES, null, values)
        db.close()
    }

    /** Delete a Folder (Cascades to Notes) */
    fun deleteFolder(folderId: Int) {
        val db = writableDatabase
        db.delete(TABLE_FOLDERS, "$COLUMN_FOLDER_ID_PRIMARY = ?", arrayOf(folderId.toString()))
        db.close()
    }

    /** Delete a Note */
    fun deleteNote(noteId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_NOTE_ID = ?", arrayOf(noteId.toString()))
        db.close()
    }

    /** Get a Specific Note by ID */
    fun getAllNotesByFolderId(folderId: Int): List<Note> {
        val notesList = mutableListOf<Note>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NOTES WHERE $COLUMN_FOLDER_ID = ?", arrayOf(folderId.toString()))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val folderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID))
            val subject = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY))
            notesList.add(Note(folderId, id, subject, date, title, content, priority))
        }

        cursor.close()
        db.close()
        return notesList
    }

    /** Get a Specific Note by ID */
    fun getNoteById(noteId: Int): Note? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NOTES WHERE $COLUMN_NOTE_ID = ?", arrayOf(noteId.toString()))

        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val folderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID))
            val subject = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY))
            cursor.close()
            Note(folderId, id, subject, date, title, content, priority)
        } else {
            cursor.close()
            null
        }
    }
}
