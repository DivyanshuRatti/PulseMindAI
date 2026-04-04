package com.example.pulsemindai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnSymptom = findViewById<CardView>(R.id.btnSymptom)
        val btnClinics = findViewById<CardView>(R.id.btnClinics)
        val btnSOS = findViewById<CardView>(R.id.btnSOS)
        val btnMedication = findViewById<CardView>(R.id.btnMedication)
        val btnHealthDashboard = findViewById<CardView>(R.id.btnHealthDashboard)

        btnSymptom.setOnClickListener {
            Toast.makeText(this, "Symptom Checker clicked", Toast.LENGTH_SHORT).show()
        }

        btnClinics.setOnClickListener {
            Toast.makeText(this, "Nearby Clinics clicked", Toast.LENGTH_SHORT).show()
        }

        btnSOS.setOnClickListener {
            Toast.makeText(this, "Emergency SOS clicked", Toast.LENGTH_SHORT).show()
        }

        btnMedication.setOnClickListener {
            Toast.makeText(this, "Medication Reminder clicked", Toast.LENGTH_SHORT).show()
        }

        btnHealthDashboard.setOnClickListener {
            Toast.makeText(this, "Health Dashboard clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
