package com.example.notesappwithsqlite

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesappwithsqlite.adapter.FolderAdapter
import com.example.notesappwithsqlite.databaseController.NoteDatabaseHelper
import com.example.notesappwithsqlite.databinding.ActivityDashboardBinding
import com.example.notesappwithsqlite.model.Folder

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: NoteDatabaseHelper
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var folderList: List<Folder>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LoadStats()
        val username = intent.getStringExtra("USERNAME") ?: "Guest"
        val userId = intent.getIntExtra("USER_ID", -1)

        val headerView = binding.navView.getHeaderView(0)
        val profileNameTextView = headerView.findViewById<TextView>(R.id.profile_name)
        profileNameTextView.text = username

        db = NoteDatabaseHelper(this)
        db.getAllFolders()

        folderList = db.getAllFolders()
        folderAdapter = FolderAdapter(folderList, this, username)
        binding.recyclerViewFolders.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFolders.adapter = folderAdapter

        binding.btnAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val ivOpenMenu: ImageView = binding.ivOpenMenu

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
            drawerLayout.closeDrawers()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LoadAddFolder(userId: Int) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_custom, null)
        val input = dialogLayout.findViewById<EditText>(R.id.editTextFolderName)
        val btnOk = dialogLayout.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogLayout.findViewById<Button>(R.id.btnCancel)

        builder.setView(dialogLayout)
        val dialog = builder.create()

        btnOk.setOnClickListener {
            val folderName = input.text.toString()
            db.insertFolder(folderName, userId, input) {
                refreshFolders()
                LoadStats()  // Update the folder count
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }

    private fun refreshFolders() {
        folderList = db.getAllFolders()
        folderAdapter.refreshData(folderList)
    }

    fun LoadStats(){
        db = NoteDatabaseHelper(this)
        val countFolder = db.countFolders()
        val countNote = db.countNotes()

        val countFolderText = binding.textView2
        val countNoteText = binding.textView3
        countFolderText.text = "$countFolder"
        countNoteText.text = "$countNote"
    }
}