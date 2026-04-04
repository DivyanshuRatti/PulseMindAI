package com.example.pulsemindai

import android.content.Intent
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
        val cvProfile = findViewById<CardView>(R.id.cvProfile)

        btnSymptom.setOnClickListener {
            val intent = Intent(this, SymptomCheckerActivity::class.java)
            startActivity(intent)
        }

        btnClinics.setOnClickListener {
            val intent = Intent(this, NearbyClinicsActivity::class.java)
            startActivity(intent)
        }

        btnSOS.setOnClickListener {
            val intent = Intent(this, SOSActivity::class.java)
            startActivity(intent)
        }

        btnMedication.setOnClickListener {
            val intent = Intent(this, MedicationActivity::class.java)
            startActivity(intent)
        }

        btnHealthDashboard.setOnClickListener {
            Toast.makeText(this, "Health Dashboard clicked", Toast.LENGTH_SHORT).show()
        }

        cvProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
