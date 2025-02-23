package com.example.notesappwithsqlite.databaseController

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.notesappwithsqlite.model.Note
import com.example.notesappwithsqlite.model.Folder

class NoteDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notesapp.db"
        private const val DATABASE_VERSION = 2

        // Notes Table
        private const val TABLE_NOTES = "allnotes"
        private const val COLUMN_NOTE_ID = "id"
        private const val COLUMN_FOLDER_ID = "folder_id" // Foreign Key
        private const val COLUMN_SUBJECT = "subject"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"

        // Folders Table
        private const val TABLE_FOLDERS = "allfolders"
        private const val COLUMN_FOLDER_ID_PRIMARY = "id"
        private const val COLUMN_FOLDER_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createFoldersTable = """
            CREATE TABLE $TABLE_FOLDERS (
                $COLUMN_FOLDER_ID_PRIMARY INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FOLDER_NAME TEXT NOT NULL
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
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FOLDERS")
        onCreate(db)
    }

    /** Insert a Folder */
    fun insertFolder(folderName: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOLDER_NAME, folderName)
        }
        val folderId = db.insert(TABLE_FOLDERS, null, values)
        db.close()
        return folderId
    }

    /** Insert a Note linked to a Folder */
    fun insertNote(note: Note, folderId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOLDER_ID, folderId)
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }
        db.insert(TABLE_NOTES, null, values)
        db.close()
    }

    /** Get All Notes by Folder */
    fun getNotesByFolder(folderId: Int): List<Note> {
        val notesList = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NOTES WHERE $COLUMN_FOLDER_ID = ?", arrayOf(folderId.toString()))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val subject = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))

            notesList.add(Note(id, subject, date, title, content))
        }

        cursor.close()
        db.close()
        return notesList
    }

    /** Get All Folders */
    fun getAllFolders(): List<Folder> {
        val folderList = mutableListOf<Folder>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FOLDERS", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID_PRIMARY))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME))
            folderList.add(Folder(id, name))
        }

        cursor.close()
        db.close()
        return folderList
    }

    /** Update Note */
    fun updateNote(note: Note) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }
        db.update(TABLE_NOTES, values, "$COLUMN_NOTE_ID = ?", arrayOf(note.id.toString()))
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
    fun getNoteById(noteID: Int): Note {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NOTES WHERE $COLUMN_NOTE_ID = ?", arrayOf(noteID.toString()))

        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val subject = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            cursor.close()
            Note(id, subject, date, title, content)
        } else {
            cursor.close()
            throw Exception("Note not found")
        }
    }
}
