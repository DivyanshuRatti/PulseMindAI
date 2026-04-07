package com.example.pulsemindai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName     = findViewById<EditText>(R.id.etName)
        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm  = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin    = findViewById<TextView>(R.id.tvLogin)

        tvLogin.setOnClickListener {
            finish() // go back to login
        }

        btnRegister.setOnClickListener {
            val name     = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm  = etConfirm.text.toString().trim()

            // Validations
            if (name.isEmpty()) {
                etName.error = "Name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (name.length < 2) {
                etName.error = "Name must be at least 2 characters"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (confirm.isEmpty()) {
                etConfirm.error = "Please confirm your password"
                etConfirm.requestFocus()
                return@setOnClickListener
            }

            if (password != confirm) {
                etConfirm.error = "Passwords do not match"
                etConfirm.requestFocus()
                return@setOnClickListener
            }

            // Check if email already registered
            val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
            val existingEmail = prefs.getString("userEmail", "")
            if (existingEmail == email) {
                etEmail.error = "This email is already registered"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Save user data
            prefs.edit()
                .putString("userName", name)
                .putString("userEmail", email)
                .putString("userPassword", password)
                .apply()

            Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_SHORT).show()

            // Go to login
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }
    }
}