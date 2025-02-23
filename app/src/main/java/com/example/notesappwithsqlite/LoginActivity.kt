package com.example.notesappwithsqlite

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.notesappwithsqlite.databaseController.UserDatabaseHelper

class LoginActivity : AppCompatActivity() {
    private lateinit var userDb: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userDb = UserDatabaseHelper(this)

        val loginBtn = findViewById<CardView>(R.id.cvLoginProceedbtn)
        val registerBtn = findViewById<TextView>(R.id.tvSignUpNowBtn)

        loginBtn.setOnClickListener {
            val username = findViewById<EditText>(R.id.etLoginUsername).text.toString().trim()
            val password = findViewById<EditText>(R.id.etLoginPassword).text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userDb.validateLogin(username, password)) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USERNAME", username)  // Passing username
                startActivity(intent)
                finish()
            }else {
                Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show()
            }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
}
