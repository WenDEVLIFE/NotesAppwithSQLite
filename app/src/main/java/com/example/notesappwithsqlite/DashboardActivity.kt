package com.example.notesappwithsqlite

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.notesappwithsqlite.databaseController.NoteDatabaseHelper
import com.example.notesappwithsqlite.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: NoteDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root) // Use binding.root instead of R.layout.activity_dashboard

        val username = intent.getStringExtra("USERNAME") ?: "Guest"
        val userId = intent.getIntExtra("USER_ID", -1) // Get userId from intent

        // Set username in Navigation Drawer header
        val headerView = binding.navView.getHeaderView(0)
        val profileNameTextView = headerView.findViewById<TextView>(R.id.profile_name)
        profileNameTextView.text = username

        db = NoteDatabaseHelper(this)

        binding.btnAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val ivOpenMenu: ImageView = binding.ivOpenMenu

        // Open Navigation Drawer when clicking the menu icon
        ivOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(binding.navView)
        }

        val floatingActionButton = binding.btnAddNote
        floatingActionButton.setOnClickListener {
            LoadAddFolder(userId)
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

    fun LoadAddFolder(userId: Int) {
        val builder = AlertDialog.Builder(this)
        // Inflate the custom layout
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_custom, null)
        val input = dialogLayout.findViewById<EditText>(R.id.editTextFolderName)
        val btnOk = dialogLayout.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogLayout.findViewById<Button>(R.id.btnCancel)

        builder.setView(dialogLayout)
        val dialog = builder.create()

        // Set up the buttons
        btnOk.setOnClickListener {
            val folderName = input.text.toString()
            db.insertFolder(folderName, userId, input)// Insert folder with userId
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }
}