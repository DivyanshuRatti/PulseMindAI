package com.example.pulsemindai

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val userName = prefs.getString("userName", "User") ?: "User"
        userEmail = prefs.getString("userEmail", "") ?: ""

        // Dynamic greeting
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning, $userName"
            hour < 17 -> "Good Afternoon, $userName"
            else -> "Good Evening, $userName"
        }
        findViewById<TextView>(R.id.tvGreeting).text = greeting

        // Profile initials
        val initials = userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("").uppercase()
        findViewById<TextView>(R.id.tvProfileInitials).text = initials

        // Dynamic reminders from SQLite
        val dbHelper = MedicationDBHelper(this)
        findViewById<TextView>(R.id.tvReminders).text =
            dbHelper.getAllMedications(userEmail).size.toString()

        // Steps from SharedPreferences
        findViewById<TextView>(R.id.tvSteps).text =
            prefs.getInt("steps", 0).toString()

        // Mood from SharedPreferences
        val tvMood = findViewById<TextView>(R.id.tvMood)
        tvMood.text = prefs.getString("mood", "😊") ?: "😊"

        // Tap steps to update
        findViewById<TextView>(R.id.tvSteps).setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter steps today"
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            input.setText(prefs.getInt("steps", 0).toString())
            AlertDialog.Builder(this)
                .setTitle("Update Steps")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val steps = input.text.toString().trim()
                    if (steps.isNotEmpty()) {
                        prefs.edit().putInt("steps", steps.toInt()).apply()
                        findViewById<TextView>(R.id.tvSteps).text = steps
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Tap mood to cycle
        val moods = listOf("😊", "😴", "😤", "😢", "😰", "🤒")
        tvMood.setOnClickListener {
            val current = tvMood.text.toString()
            val next = moods[(moods.indexOf(current) + 1) % moods.size]
            tvMood.text = next
            prefs.edit().putString("mood", next).apply()
        }

        // Navigation
        findViewById<CardView>(R.id.btnSymptom).setOnClickListener {
            startActivity(Intent(this, SymptomCheckerActivity::class.java))
        }
        findViewById<CardView>(R.id.btnClinics).setOnClickListener {
            startActivity(Intent(this, NearbyClinicsActivity::class.java))
        }
        findViewById<CardView>(R.id.btnSOS).setOnClickListener {
            startActivity(Intent(this, SOSActivity::class.java))
        }
        findViewById<CardView>(R.id.btnMedication).setOnClickListener {
            startActivity(Intent(this, MedicationActivity::class.java))
        }
        findViewById<CardView>(R.id.btnHealthDashboard).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<CardView>(R.id.cvProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning from other screens
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val dbHelper = MedicationDBHelper(this)
        findViewById<TextView>(R.id.tvReminders).text =
            dbHelper.getAllMedications(userEmail).size.toString()
        findViewById<TextView>(R.id.tvSteps).text =
            prefs.getInt("steps", 0).toString()
        findViewById<TextView>(R.id.tvMood).text =
            prefs.getString("mood", "😊") ?: "😊"
    }
}