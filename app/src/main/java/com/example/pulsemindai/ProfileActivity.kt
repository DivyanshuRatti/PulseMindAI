package com.example.pulsemindai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val userName  = prefs.getString("userName", "User") ?: "User"
        val userEmail = prefs.getString("userEmail", "") ?: ""
        val steps     = prefs.getInt("steps", 0)

        // Initials for avatar
        val initials = userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("").uppercase()

        // Set avatar and name
        findViewById<TextView>(R.id.tvAvatar).text = initials
        findViewById<TextView>(R.id.tvUserName).text = userName
        findViewById<TextView>(R.id.tvUserEmail).text = userEmail
        findViewById<TextView>(R.id.tvInfoName).text = userName
        findViewById<TextView>(R.id.tvInfoEmail).text = userEmail
        findViewById<TextView>(R.id.tvStepsProfile).text = steps.toString()

        // Medication count
        val dbHelper = MedicationDBHelper(this)
        val medCount = dbHelper.getAllMedications(userEmail).size
        findViewById<TextView>(R.id.tvDaysActive).text = medCount.toString()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}