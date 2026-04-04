package com.example.pulsemindai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Greeting based on time
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning, User"
            hour < 17 -> "Good Afternoon, User"
            else -> "Good Evening, User"
        }
        findViewById<TextView>(R.id.tvGreeting).text = greeting

        // Feature cards
        findViewById<LinearLayout>(R.id.cardSymptom).setOnClickListener {
            startActivity(Intent(this, SymptomCheckerActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardClinics).setOnClickListener {
            startActivity(Intent(this, NearbyClinicsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardSOS).setOnClickListener {
            startActivity(Intent(this, SOSActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardMedication).setOnClickListener {
            startActivity(Intent(this, MedicationActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardHealthTracker).setOnClickListener {
            startActivity(Intent(this, HealthTrackerActivity::class.java))
        }

    }
}