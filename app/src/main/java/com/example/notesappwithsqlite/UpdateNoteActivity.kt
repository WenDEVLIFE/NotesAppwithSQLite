package com.example.notesappwithsqlite

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notesappwithsqlite.databinding.ActivityUpdateNoteBinding
import java.util.*

class UpdateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var db: NoteDatabaseHelper
    private var noteID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabaseHelper(this)
        noteID = intent.getIntExtra("note_id", -1)

        if (noteID == -1) {
            finish()
            return
        }

        val note = db.getNoteById(noteID)
        binding.etUpdateTitle.setText(note.title)
        binding.etUpdateDescription.setText(note.content)
        binding.etUpdateDate.setText(note.date) // Ensure Note model includes `date`
        binding.etUpdateSubjectTitle.setText(note.subject) // Ensure Note model includes `subject`

        // Date Picker Dialog
        binding.etUpdateDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                binding.etUpdateDate.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        binding.updateSaveButton.setOnClickListener {
            val newTitle = binding.etUpdateTitle.text.toString().trim()
            val newContent = binding.etUpdateDescription.text.toString().trim()
            val newDate = binding.etUpdateDate.text.toString().trim()
            val newSubjectTitle = binding.etUpdateSubjectTitle.text.toString().trim()

            if (newTitle.isEmpty() || newContent.isEmpty() || newDate.isEmpty() || newSubjectTitle.isEmpty()) {
                Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedNote = Note(noteID, newSubjectTitle, newDate, newTitle, newContent) // Correct order
            db.updateNote(updatedNote)

            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
