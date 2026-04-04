package com.example.pulsemindai

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SymptomCheckerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_checker)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etSymptoms = findViewById<EditText>(R.id.etSymptoms)
        val spinnerSeverity = findViewById<Spinner>(R.id.spinnerSeverity)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)

        // Setup Spinner
        val severityLevels = arrayOf("Mild", "Moderate", "Severe")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, severityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeverity.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        btnAnalyze.setOnClickListener {
            val symptoms = etSymptoms.text.toString()
            val severity = spinnerSeverity.selectedItem.toString()

            if (symptoms.isEmpty()) {
                Toast.makeText(this, "Please describe your symptoms", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple "AI" prediction logic for now
            val advice = when (severity) {
                "Mild" -> "Rest and stay hydrated. Monitor your symptoms for the next 24 hours."
                "Moderate" -> "Consider scheduling a consultation with a healthcare provider."
                "Severe" -> "URGENT: Please visit the nearest emergency clinic immediately."
                else -> "Please consult a medical professional."
            }

            Toast.makeText(this, "AI Analysis: $advice", Toast.LENGTH_LONG).show()
        }
    }
}
