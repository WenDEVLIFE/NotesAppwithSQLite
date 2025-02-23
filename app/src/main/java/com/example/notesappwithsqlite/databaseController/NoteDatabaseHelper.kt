package com.example.notesappwithsqlite.databaseController

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.notesappwithsqlite.model.Note
import com.example.notesappwithsqlite.model.Folder
import java.time.LocalDateTime

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
        private const val COLUMN_USER_ID = "user_id" // Add userId column
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createFoldersTable = """
            CREATE TABLE $TABLE_FOLDERS (
                $COLUMN_FOLDER_ID_PRIMARY INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FOLDER_NAME TEXT NOT NULL,
                $COLUMN_USER_ID INTEGER NOT NULL
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

    /** Get All Folders by User */
    fun getFoldersByUser(userId: Int): List<Folder> {
        val folderList = mutableListOf<Folder>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FOLDERS WHERE $COLUMN_USER_ID = ?", arrayOf(userId.toString()))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID_PRIMARY))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            folderList.add(Folder(id, name, date, userId))
        }

        cursor.close()
        db.close()
        return folderList
    }

    /** Insert a Note linked to a Folder */
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertFolder(folderName: String, userId: Int?, input: EditText) : Long {
       try {
           val db = writableDatabase

           val localDate = LocalDateTime.now()
           var date = localDate.toString()
           val values = ContentValues().apply {
               put(COLUMN_FOLDER_NAME, folderName)
               put(COLUMN_DATE, date)  // Include date
               put(COLUMN_USER_ID, userId)
           }
           val folderId = db.insert(TABLE_FOLDERS, null, values)
           db.close()
           input.setText("")
           Toast.makeText(input.context, "Folder added successfully!", Toast.LENGTH_SHORT).show()
           return folderId
        } catch (e: Exception) {
            Toast.makeText(input.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            return -1
        }
    }

    /** Get All Folders */
    fun getAllFolders(userId: Int): List<Folder> {

        val folderList = mutableListOf<Folder>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FOLDERS WHERE $COLUMN_USER_ID = ?", arrayOf(userId.toString()))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID_PRIMARY))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))  // Now valid
            folderList.add(Folder(id, name, date, userId))
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

    fun insertNote(note: Note, i: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
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
