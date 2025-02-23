package com.example.notesappwithsqlite

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesappwithsqlite.adapter.NotesAdapter
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
}