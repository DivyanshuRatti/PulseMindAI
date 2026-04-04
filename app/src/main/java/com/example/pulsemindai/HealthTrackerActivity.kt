package com.example.pulsemindai

import android.app.AlertDialog
import android.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*

class HealthTrackerActivity : AppCompatActivity() {

    private lateinit var dbHelper: HealthTrackerDBHelper
    private lateinit var tvDate: TextView
    private lateinit var tvStepsValue: TextView
    private lateinit var tvWaterValue: TextView
    private lateinit var tvSleepValue: TextView
    private lateinit var tvCaloriesValue: TextView
    private lateinit var tvWeightValue: TextView
    private lateinit var tvHeartRateValue: TextView
    private lateinit var tvHealthTip: TextView
    private lateinit var tvTipLoading: TextView
    private lateinit var tipCard: LinearLayout

    // Current values
    private var steps = 0
    private var water = 0
    private var sleep = 0.0
    private var calories = 0
    private var weight = 0.0
    private var heartRate = 0

    // Gemini API key - same one you used for symptom checker
    private val API_KEY = "YOUR_GEMINI_API_KEY_HERE"

    // Today's date as string
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_tracker)

        dbHelper = HealthTrackerDBHelper(this)

        // Link all views
        tvDate = findViewById(R.id.tvDate)
        tvStepsValue = findViewById(R.id.tvStepsValue)
        tvWaterValue = findViewById(R.id.tvWaterValue)
        tvSleepValue = findViewById(R.id.tvSleepValue)
        tvCaloriesValue = findViewById(R.id.tvCaloriesValue)
        tvWeightValue = findViewById(R.id.tvWeightValue)
        tvHeartRateValue = findViewById(R.id.tvHeartRateValue)
        tvHealthTip = findViewById(R.id.tvHealthTip)
        tvTipLoading = findViewById(R.id.tvTipLoading)
        tipCard = findViewById(R.id.tipCard)

        // Show today's date nicely
        val dateFormat = SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())
        tvDate.text = "📅 ${dateFormat.format(Date())}"

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        // + buttons for each metric
        findViewById<Button>(R.id.btnEditSteps).setOnClickListener {
            showInputDialog("Steps", "How many steps today?", false) { value ->
                steps = value.toInt()
                tvStepsValue.text = "$steps / 10000 steps"
            }
        }

        findViewById<Button>(R.id.btnEditWater).setOnClickListener {
            showInputDialog("Water", "How many glasses of water?", false) { value ->
                water = value.toInt()
                tvWaterValue.text = "$water / 8 glasses"
            }
        }

        findViewById<Button>(R.id.btnEditSleep).setOnClickListener {
            showInputDialog("Sleep", "How many hours did you sleep?", true) { value ->
                sleep = value
                tvSleepValue.text = "$sleep / 8 hours"
            }
        }

        findViewById<Button>(R.id.btnEditCalories).setOnClickListener {
            showInputDialog("Calories", "How many calories did you eat?", false) { value ->
                calories = value.toInt()
                tvCaloriesValue.text = "$calories / 2000 kcal"
            }
        }

        findViewById<Button>(R.id.btnEditWeight).setOnClickListener {
            showInputDialog("Weight", "What is your weight in kg?", true) { value ->
                weight = value
                tvWeightValue.text = "$weight kg"
            }
        }

        findViewById<Button>(R.id.btnEditHeartRate).setOnClickListener {
            showInputDialog("Heart Rate", "What is your heart rate (bpm)?", false) { value ->
                heartRate = value.toInt()
                tvHeartRateValue.text = "$heartRate / 70 bpm"
            }
        }

        // Save button
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveData()
        }

        // Load today's existing data if any
        loadTodayData()
    }

    private fun loadTodayData() {
        val data = dbHelper.getTodayData(today)
        if (data != null) {
            steps = data.steps
            water = data.water
            sleep = data.sleep
            calories = data.calories
            weight = data.weight
            heartRate = data.heartRate

            tvStepsValue.text = "$steps / 10000 steps"
            tvWaterValue.text = "$water / 8 glasses"
            tvSleepValue.text = "$sleep / 8 hours"
            tvCaloriesValue.text = "$calories / 2000 kcal"
            tvWeightValue.text = "$weight kg"
            tvHeartRateValue.text = "$heartRate / 70 bpm"
        }
    }

    private fun saveData() {
        // Save to SQLite
        dbHelper.saveTodayData(today, steps, water, sleep, calories, weight, heartRate)
        Toast.makeText(this, "✅ Data saved!", Toast.LENGTH_SHORT).show()

        // Now generate AI health tip based on saved data
        generateHealthTip()
    }

    private fun generateHealthTip() {
        // Show tip card with loading
        tipCard.visibility = View.VISIBLE
        tvTipLoading.visibility = View.VISIBLE
        tvHealthTip.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tip = callGeminiForTip()

                withContext(Dispatchers.Main) {
                    tvTipLoading.visibility = View.GONE
                    tvHealthTip.visibility = View.VISIBLE
                    tvHealthTip.text = tip
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvTipLoading.visibility = View.GONE
                    tvHealthTip.visibility = View.VISIBLE
                    tvHealthTip.text = "Stay hydrated and keep moving today! 💪"
                }
            }
        }
    }

    private fun callGeminiForTip(): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val prompt = """
            You are a personal health coach in a mobile app.
            Here is the user's health data for today:
            - Steps: $steps / 10000 goal
            - Water intake: $water / 8 glasses
            - Sleep: $sleep / 8 hours
            - Calories: $calories / 2000 kcal
            - Weight: $weight kg
            - Heart rate: $heartRate bpm
            
            Based on what they are lacking or doing well,
            give ONE short encouraging personalized health tip.
            Keep it to 2-3 sentences max.
            Make it warm and friendly, not robotic.
        """.trimIndent()

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

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(requestBody.toString())
        writer.flush()

        val responseCode = connection.responseCode
        val response = if (responseCode == 200) {
            connection.inputStream.bufferedReader().readText()
        } else {
            throw Exception("API Error $responseCode")
        }

        connection.disconnect()

        val jsonResponse = JSONObject(response)
        return jsonResponse
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    private fun showInputDialog(
        title: String,
        message: String,
        isDecimal: Boolean,
        onSave: (Double) -> Unit
    ) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 10)

        val etValue = EditText(this)
        etValue.hint = if (isDecimal) "e.g. 7.5" else "e.g. 8000"
        etValue.inputType = if (isDecimal)
            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or android.text.InputType.TYPE_CLASS_NUMBER
        else
            android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etValue)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val input = etValue.text.toString().trim()
                if (input.isEmpty()) {
                    Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                try {
                    onSave(input.toDouble())
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}