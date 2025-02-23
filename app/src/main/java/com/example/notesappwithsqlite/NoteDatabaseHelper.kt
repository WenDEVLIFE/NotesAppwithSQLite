package com.example.notesappwithsqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notesapp.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "allnotes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_SUBJECT = "subject"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
            $COLUMN_TITLE TEXT,  -- ✅ First
            $COLUMN_CONTENT TEXT, -- ✅ Second
            $COLUMN_DATE TEXT,    -- ✅ Third
            $COLUMN_SUBJECT TEXT  -- ✅ Fourth
        )
    """.trimIndent()
        db?.execSQL(createTableQuery)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertNote(note: Note) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllNotes(): List<Note> {
        val notesList = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
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

    fun updateNote(note: Note) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUBJECT, note.subject)
            put(COLUMN_DATE, note.date)
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(note.id.toString()))
        db.close()
    }

    fun deleteNote(noteId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(noteId.toString()))
        db.close()
    }

    fun getNoteCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getNoteById(noteID: Int): Note {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM allnotes WHERE id = ?", arrayOf(noteID.toString()))

        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
            cursor.close()
            Note(id, subject, date, title, content) // CORRECT ORDER
        } else {
            cursor.close()
            throw Exception("Note not found")
        }
    }
}
