package com.example.pulsemindai

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ReportsActivity : AppCompatActivity() {

    private lateinit var reportsContainer: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var tvMedCount: TextView
    private lateinit var tvReportCount: TextView

    private val uploadedReports = mutableListOf<Pair<String, Uri>>()

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = getFileName(uri) ?: "Report_${uploadedReports.size + 1}"
                uploadedReports.add(Pair(fileName, uri))
                updateReportsList()
                Toast.makeText(this, "$fileName uploaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        reportsContainer = findViewById(R.id.reportsContainer)
        tvEmpty          = findViewById(R.id.tvEmpty)
        tvMedCount       = findViewById(R.id.tvMedCount)
        tvReportCount    = findViewById(R.id.tvReportCount)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnUpload).setOnClickListener {
            openFilePicker()
        }

        loadMedicationCount()
    }

    private fun loadMedicationCount() {
        val userEmail = getSharedPreferences("UserProfile", MODE_PRIVATE)
            .getString("userEmail", "") ?: ""
        val dbHelper = MedicationDBHelper(this)
        val count = dbHelper.getAllMedications(userEmail).size
        tvMedCount.text = count.toString()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "image/jpeg",
                "image/png",
                "image/jpg"
            ))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickFileLauncher.launch(Intent.createChooser(intent, "Select Report"))
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) name = it.getString(nameIndex)
            }
        }
        return name
    }

    private fun updateReportsList() {
        reportsContainer.removeAllViews()
        tvReportCount.text = uploadedReports.size.toString()

        if (uploadedReports.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            return
        }

        tvEmpty.visibility = View.GONE

        for ((name, uri) in uploadedReports) {
            addReportCard(name, uri)
        }
    }

    private fun addReportCard(fileName: String, uri: Uri) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 0, 0, 12) }
            setPadding(16, 16, 16, 16)
        }

        card.addView(TextView(this).apply {
            text = if (fileName.endsWith(".pdf")) "📄" else "🖼️"
            textSize = 28f
            setPadding(0, 0, 16, 0)
        })

        card.addView(TextView(this).apply {
            text = fileName
            textSize = 14f
            setTextColor(Color.parseColor("#0A3D62"))
            paint.isFakeBoldText = true
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        })

        card.addView(Button(this).apply {
            text = "View"
            textSize = 12f
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#4A90E2")
            )
            setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, contentResolver.getType(uri))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@ReportsActivity,
                        "No app found to open this file", Toast.LENGTH_SHORT).show()
                }
            }
        })

        card.addView(Button(this).apply {
            text = "🗑️"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#E74C3C"))
            setOnClickListener {
                uploadedReports.removeAll { it.first == fileName }
                updateReportsList()
                Toast.makeText(this@ReportsActivity, "Removed!", Toast.LENGTH_SHORT).show()
            }
        })

        reportsContainer.addView(card)
    }
}