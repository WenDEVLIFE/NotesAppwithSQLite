package com.example.notesappwithsqlite

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notesappwithsqlite.databaseController.UserDatabaseHelper
import com.google.android.material.card.MaterialCardView

class SignUpActivity : AppCompatActivity() {
    private lateinit var userDb: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        userDb = UserDatabaseHelper(this)

        val registerBtn = findViewById<CardView>(R.id.cvSignupProceedbtn)
        val loginBtn = findViewById<TextView>(R.id.tvLoginHereBtn)

        registerBtn.setOnClickListener {
            val username = findViewById<EditText>(R.id.etSignupUsername).text.toString().trim()
            val ageText = findViewById<EditText>(R.id.etSignupAge).text.toString().trim()
            val password = findViewById<EditText>(R.id.etSignupPassword).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.etSignupConfirmPassword).text.toString()

            if (username.isEmpty() || ageText.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            if (age == null) {
                Toast.makeText(this, "Enter a valid age!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userDb.isUserExists(username)) {
                Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = userDb.registerUser(username, age, password)
            if (success) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration Failed!", Toast.LENGTH_SHORT).show()
            }
        }

        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
