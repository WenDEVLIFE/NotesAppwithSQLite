package com.example.notesappwithsqlite

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesappwithsqlite.databinding.ActivityMainBinding
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.notesappwithsqlite.adapter.NotesAdapter
import com.example.notesappwithsqlite.databaseController.NoteDatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NoteDatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    var folderID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("USERNAME") ?: "Guest"
        folderID = intent.getIntExtra("FOLDER_ID", 0)
        print("Folder ID: $folderID")
        print("Username: $username")
        Toast.makeText(this, "Folder ID: $folderID", Toast.LENGTH_SHORT).show()

        // Set username in Navigation Drawer header
        val headerView = binding.navView.getHeaderView(0)
        val profileNameTextView = headerView.findViewById<TextView>(R.id.profile_name)
        profileNameTextView.text = username

        db = NoteDatabaseHelper(this)
        refreshNotes()

        binding.btnAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("FOLDER_ID", folderID) // Pass folderID to AddNoteActivity
            startActivity(intent)
        }

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = notesAdapter

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val ivOpenMenu: ImageView = findViewById(R.id.ivOpenMenu)

        // Open Navigation Drawer when clicking the menu icon
        ivOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(binding.navView)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    val intent = Intent(this, SplashActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawers() // Close drawer after selection
            true
        }
    }

    override fun onResume() {
        super.onResume()
        refreshNotes()
        createNotificationChannel()
        sendNotification("We will remind you the notes a day before!")
    }

    private fun refreshNotes() {
        val notes = db.getAllNotesByFolderId(folderID)
        notesAdapter = NotesAdapter(notes, this)
        binding.rvNotes.adapter = notesAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) { // Matches request code
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id", // Unique Channel ID
                "Notes Notifications", // Channel Name
                NotificationManager.IMPORTANCE_HIGH // High importance for visibility
            ).apply {
                description = "Channel for note reminders"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(noteTitle: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, "channel_id") // Same channel_id
            .setSmallIcon(R.drawable.ic_notification) // Change to a real drawable
            .setContentTitle("Welcome to AcadPlanner!")
            .setContentText("Your note '$noteTitle' has been saved successfully!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}