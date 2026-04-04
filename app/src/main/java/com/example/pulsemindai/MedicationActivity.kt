package com.example.pulsemindai

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MedicationActivity : AppCompatActivity() {

    private lateinit var dbHelper: MedicationDBHelper
    private lateinit var medicationsContainer: LinearLayout
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication)

        // Create DBHelper - connects to SQLite
        dbHelper = MedicationDBHelper(this)

        medicationsContainer = findViewById(R.id.medicationsContainer)
        tvCount = findViewById(R.id.tvCount)
        tvEmpty = findViewById(R.id.tvEmpty)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        // Add button
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            showAddMedicationDialog()
        }

        // Load medications from database
        loadMedications()
    }

    private fun loadMedications() {
        // Get all from SQLite
        val medications = dbHelper.getAllMedications()

        // Clear current list on screen
        medicationsContainer.removeAllViews()

        // Update count
        tvCount.text = "Your Medications (${medications.size})"

        // Show empty state if no medications
        if (medications.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            // Add each medication as a card
            for (med in medications) {
                addMedicationCard(med)
            }
        }
    }

    private fun addMedicationCard(med: Medication) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.WHITE)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 12)
        card.layoutParams = params
        card.setPadding(20, 16, 20, 16)

        // Top row: name + delete button
        val topRow = LinearLayout(this)
        topRow.orientation = LinearLayout.HORIZONTAL
        topRow.gravity = android.view.Gravity.CENTER_VERTICAL

        val tvName = TextView(this)
        tvName.text = "💊 ${med.name}"
        tvName.textSize = 16f
        tvName.setTextColor(Color.parseColor("#0A3D62"))
        tvName.paint.isFakeBoldText = true
        val nameParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvName.layoutParams = nameParams
        topRow.addView(tvName)

        // Delete button
        val btnDelete = Button(this)
        btnDelete.text = "🗑️"
        btnDelete.textSize = 16f
        btnDelete.setBackgroundColor(Color.TRANSPARENT)
        btnDelete.setTextColor(Color.parseColor("#E74C3C"))
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Medication")
                .setMessage("Delete ${med.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    dbHelper.deleteMedication(med.id)
                    loadMedications()
                    Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        topRow.addView(btnDelete)
        card.addView(topRow)

        // Dosage
        val tvDosage = TextView(this)
        tvDosage.text = med.dosage
        tvDosage.textSize = 13f
        tvDosage.setTextColor(Color.parseColor("#888888"))
        tvDosage.setPadding(0, 4, 0, 8)
        card.addView(tvDosage)

        // Bottom row: time + toggle
        val bottomRow = LinearLayout(this)
        bottomRow.orientation = LinearLayout.HORIZONTAL
        bottomRow.gravity = android.view.Gravity.CENTER_VERTICAL

        val tvTime = TextView(this)
        tvTime.text = "⏰ ${med.time}"
        tvTime.textSize = 13f
        tvTime.setTextColor(Color.parseColor("#555555"))
        val timeParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvTime.layoutParams = timeParams
        bottomRow.addView(tvTime)

        // Toggle switch
        val toggle = Switch(this)
        toggle.isChecked = med.isEnabled
        toggle.setOnCheckedChangeListener { _, isChecked ->
            dbHelper.updateEnabled(med.id, isChecked)
            Toast.makeText(
                this,
                if (isChecked) "${med.name} reminder ON" else "${med.name} reminder OFF",
                Toast.LENGTH_SHORT
            ).show()
        }
        bottomRow.addView(toggle)
        card.addView(bottomRow)

        medicationsContainer.addView(card)
    }

    private fun showAddMedicationDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 10)

        // Medicine name field
        val etName = EditText(this)
        etName.hint = "Medicine name (e.g. Vitamin D)"
        layout.addView(etName)

        // Dosage field
        val etDosage = EditText(this)
        etDosage.hint = "Dosage (e.g. 1000 IU, 10mg)"
        layout.addView(etDosage)

        // Time field - shows time picker
        val etTime = EditText(this)
        etTime.hint = "Time (tap to pick)"
        etTime.isFocusable = false
        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                etTime.setText(String.format("%02d:%02d %s", displayHour, minute, amPm))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }
        layout.addView(etTime)

        AlertDialog.Builder(this)
            .setTitle("💊 Add Medication")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val dosage = etDosage.text.toString().trim()
                val time = etTime.text.toString().trim()

                if (name.isEmpty() || dosage.isEmpty() || time.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Save to SQLite database
                dbHelper.addMedication(name, dosage, time)
                loadMedications()
                Toast.makeText(this, "✅ Medication saved!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}