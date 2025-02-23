package com.example.notesappwithsqlite

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notesappwithsqlite.databaseController.NoteDatabaseHelper
import com.example.notesappwithsqlite.databaseController.ReminderReceiver
import com.example.notesappwithsqlite.databinding.ActivityAddNoteBinding
import com.example.notesappwithsqlite.model.Note
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: NoteDatabaseHelper
    private var selectedDate: String = ""  // Stores selected date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabaseHelper(this)

        // Date Picker Dialog on click of etDate
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // Save Button Click
        binding.saveButton.setOnClickListener {
            saveNote()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                // Formatting the selected date
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                binding.etDate.setText(formattedDate)
                selectedDate = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Restrict past dates
        datePicker.datePicker.minDate = calendar.timeInMillis

        datePicker.show()
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val subjectTitle = binding.etSubjectTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || subjectTitle.isEmpty() || description.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val note = Note(0, subjectTitle, selectedDate, title, description)
        db.insertNote(note , 1) // 1 is the folder id
        Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show()

        // 🔔 Immediate notification
        scheduleImmediateNotification("$title is now saved and will remind you 1 day before!", description)

        // 📅 Schedule notification 1 day before
        scheduleNotification("Reminder! Your note: $title is scheduled today!", description, selectedDate)

        finish()
    }

    private fun scheduleImmediateNotification(title: String, message: String) {
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        sendBroadcast(intent) // 🔥 Immediately trigger the notification
    }

    private fun scheduleNotification(title: String, message: String, date: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Convert date to Calendar object
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = dateFormat.parse(date)
        if (selectedDate != null) {
            calendar.time = selectedDate
            calendar.add(Calendar.DAY_OF_YEAR, -1) // 🔔 Set for 1 day before
            calendar.set(Calendar.HOUR_OF_DAY, 9) // 📅 Set time to 9 AM
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        }

        // Set the alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
