package com.example.pulsemindai

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pulsemindai.Medication
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SymptomCheckerActivity : AppCompatActivity() {

    private lateinit var etSymptoms: EditText
    private lateinit var spinnerSeverity: Spinner
    private lateinit var btnAnalyze: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvResult: TextView
    private lateinit var resultCard: LinearLayout
    private lateinit var tvLoading: TextView

    private val GROQ_API_KEY = BuildConfig.GROQ_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_checker)

        etSymptoms      = findViewById(R.id.etSymptoms)
        spinnerSeverity = findViewById(R.id.spinnerSeverity)
        btnAnalyze      = findViewById(R.id.btnAnalyze)
        btnBack         = findViewById(R.id.btnBack)
        tvResult        = findViewById(R.id.tvResult)
        resultCard      = findViewById(R.id.resultCard)
        tvLoading       = findViewById(R.id.tvLoading)

        btnBack.setOnClickListener { finish() }

        val severityOptions = listOf("Mild", "Moderate", "Severe")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, severityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeverity.adapter = adapter

        btnAnalyze.setOnClickListener {
            val symptoms = etSymptoms.text.toString().trim()
            if (symptoms.isEmpty()) {
                Toast.makeText(this, "Please describe your symptoms first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            analyzeSymptoms(symptoms, spinnerSeverity.selectedItem.toString())
        }
    }

    private fun analyzeSymptoms(symptoms: String, severity: String) {
        tvLoading.visibility  = View.VISIBLE
        resultCard.visibility = View.GONE
        btnAnalyze.isEnabled  = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = callGroqAPI(symptoms, severity)
                withContext(Dispatchers.Main) {
                    tvLoading.visibility  = View.GONE
                    resultCard.visibility = View.VISIBLE
                    btnAnalyze.isEnabled  = true
                    tvResult.text = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvLoading.visibility = View.GONE
                    btnAnalyze.isEnabled = true
                    Toast.makeText(
                        this@SymptomCheckerActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun callGroqAPI(symptoms: String, severity: String): String {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $GROQ_API_KEY")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val prompt = """
            You are a helpful medical assistant in a health app.
            The user has these symptoms: $symptoms
            Severity level: $severity
            
            Please provide in simple language:
            1. Possible causes
            2. What they should do (home care or see a doctor?)
            3. Urgency: LOW / MEDIUM / HIGH
            
            Keep it short and easy to understand.
            Always remind them to consult a real doctor for serious issues.
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 512)
        }

        OutputStreamWriter(connection.outputStream).use {
            it.write(requestBody.toString())
            it.flush()
        }

        val response = if (connection.responseCode == 200)
            connection.inputStream.bufferedReader().readText()
        else {
            val error = connection.errorStream?.bufferedReader()?.readText()
            throw Exception("API Error ${connection.responseCode}: $error")
        }
        connection.disconnect()

        return JSONObject(response)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}

