package com.example.pulsemindai

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.pulsemindai.BuildConfig

class SymptomCheckerActivity : AppCompatActivity() {

    private lateinit var etSymptoms: EditText
    private lateinit var spinnerSeverity: Spinner
    private lateinit var btnAnalyze: Button
    private lateinit var tvResult: TextView
    private lateinit var resultCard: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var btnBack: TextView

    // Paste your Gemini API key here
    private val API_KEY = BuildConfig.GEMINI_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_checker)

        etSymptoms = findViewById(R.id.etSymptoms)
        spinnerSeverity = findViewById(R.id.spinnerSeverity)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        tvResult = findViewById(R.id.tvResult)
        resultCard = findViewById(R.id.resultCard)
        tvLoading = findViewById(R.id.tvLoading)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // Severity dropdown
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

            val severity = spinnerSeverity.selectedItem.toString()
            analyzeSymptoms(symptoms, severity)
        }
    }

    private fun analyzeSymptoms(symptoms: String, severity: String) {
        tvLoading.visibility = View.VISIBLE
        resultCard.visibility = View.GONE
        btnAnalyze.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Wait 2 seconds before calling API
                delay(2000)
                val result = callGeminiAPI(symptoms, severity)

                withContext(Dispatchers.Main) {
                    tvLoading.visibility = View.GONE
                    resultCard.visibility = View.VISIBLE
                    btnAnalyze.isEnabled = true
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

    private fun callGeminiAPI(symptoms: String, severity: String): String {

        // Gemini API URL
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        // The prompt we send to Gemini
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

        // Build JSON request body - this is what Gemini expects
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        // Send request
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(requestBody.toString())
        writer.flush()

        // Read response
        val responseCode = connection.responseCode
        val response = if (responseCode == 200) {
            connection.inputStream.bufferedReader().readText()
        } else {
            val error = connection.errorStream.bufferedReader().readText()
            throw Exception("API Error $responseCode: $error")
        }

        connection.disconnect()

        // Parse Gemini response to get just the text
        // Gemini response structure:
        // candidates -> [0] -> content -> parts -> [0] -> text
        val jsonResponse = JSONObject(response)
        return jsonResponse
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }
}