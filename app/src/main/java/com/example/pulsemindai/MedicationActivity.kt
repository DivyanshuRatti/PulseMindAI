package com.example.pulsemindai

import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.*

class MedicationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnAddMedication = findViewById<CardView>(R.id.btnAddMedication)

        btnBack.setOnClickListener {
            finish()
        }

        btnAddMedication.setOnClickListener {
            showAddMedicationDialog()
        }

        // Setup delete button for existing items (demo)
        setupDeleteButtons()
    }

    private fun setupDeleteButtons() {
        val btnDelete1 = findViewById<ImageButton>(R.id.btnDeleteMed)
        // Since we are using <include>, we'd typically use a RecyclerView. 
        // For this demo, let's just apply it to the first one.
        btnDelete1?.setOnClickListener {
            Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddMedicationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_add_medication)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etMedName = dialog.findViewById<EditText>(R.id.etMedName)
        val etDosage = dialog.findViewById<EditText>(R.id.etDosage)
        val etTime = dialog.findViewById<EditText>(R.id.etTime)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                etTime.setText(String.format("%02d:%02d %s", hourFormatted, selectedMinute, amPm))
            }, hour, minute, false)
            timePickerDialog.show()
        }

        btnAdd.setOnClickListener {
            val name = etMedName.text.toString()
            if (name.isNotEmpty()) {
                Toast.makeText(this, "$name added to reminders", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter medication name", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
