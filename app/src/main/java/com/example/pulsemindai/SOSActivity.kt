package com.example.pulsemindai

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class SOSActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvLocation: TextView
    private lateinit var tvContact1Name: TextView
    private lateinit var tvContact1Phone: TextView
    private lateinit var tvContact2Name: TextView
    private lateinit var tvContact2Phone: TextView

    private var currentLat = 0.0
    private var currentLng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        // SharedPreferences to save contacts
        sharedPreferences = getSharedPreferences("SOSContacts", MODE_PRIVATE)

        // Link views
        tvLocation = findViewById(R.id.tvLocation)
        tvContact1Name = findViewById(R.id.tvContact1Name)
        tvContact1Phone = findViewById(R.id.tvContact1Phone)
        tvContact2Name = findViewById(R.id.tvContact2Name)
        tvContact2Phone = findViewById(R.id.tvContact2Phone)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        // Load saved contacts
        loadContacts()

        // Get location
        getLocation()

        // Big SOS button - calls 911
        findViewById<Button>(R.id.btnSOS).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("🚨 Call Emergency Services?")
                .setMessage("This will call 911. Are you sure?")
                .setPositiveButton("Yes, Call 911") { _, _ ->
                    callNumber("911")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Call 911 button
        findViewById<Button>(R.id.btnCall911).setOnClickListener {
            callNumber("911")
        }

        // Call Contact 1
        findViewById<Button>(R.id.btnCallContact1).setOnClickListener {
            val phone = sharedPreferences.getString("contact1Phone", "") ?: ""
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please set Contact 1 first", Toast.LENGTH_SHORT).show()
            } else {
                callNumber(phone)
            }
        }

        // Call Contact 2
        findViewById<Button>(R.id.btnCallContact2).setOnClickListener {
            val phone = sharedPreferences.getString("contact2Phone", "") ?: ""
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please set Contact 2 first", Toast.LENGTH_SHORT).show()
            } else {
                callNumber(phone)
            }
        }

        // Edit Contact 1
        findViewById<Button>(R.id.btnEditContact1).setOnClickListener {
            showEditContactDialog(1)
        }

        // Edit Contact 2
        findViewById<Button>(R.id.btnEditContact2).setOnClickListener {
            showEditContactDialog(2)
        }

        // Share location
        findViewById<Button>(R.id.btnShareLocation).setOnClickListener {
            shareLocation()
        }
    }

    private fun loadContacts() {
        val name1 = sharedPreferences.getString("contact1Name", "Contact 1") ?: "Contact 1"
        val phone1 = sharedPreferences.getString("contact1Phone", "Not set") ?: "Not set"
        val name2 = sharedPreferences.getString("contact2Name", "Contact 2") ?: "Contact 2"
        val phone2 = sharedPreferences.getString("contact2Phone", "Not set") ?: "Not set"

        tvContact1Name.text = "👤 $name1"
        tvContact1Phone.text = phone1
        tvContact2Name.text = "👤 $name2"
        tvContact2Phone.text = phone2
    }

    private fun showEditContactDialog(contactNumber: Int) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)

        val etName = EditText(this)
        etName.hint = "Contact Name (e.g. Mom)"
        val currentName = sharedPreferences.getString("contact${contactNumber}Name", "") ?: ""
        etName.setText(currentName)
        layout.addView(etName)

        val etPhone = EditText(this)
        etPhone.hint = "Phone Number"
        etPhone.inputType = android.text.InputType.TYPE_CLASS_PHONE
        val currentPhone = sharedPreferences.getString("contact${contactNumber}Phone", "") ?: ""
        etPhone.setText(currentPhone)
        layout.addView(etPhone)

        AlertDialog.Builder(this)
            .setTitle("Edit Contact $contactNumber")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Save to SharedPreferences
                sharedPreferences.edit()
                    .putString("contact${contactNumber}Name", name)
                    .putString("contact${contactNumber}Phone", phone)
                    .apply()

                // Reload contacts on screen
                loadContacts()
                Toast.makeText(this, "Contact saved!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            tvLocation.text = "📍 Location permission not granted"
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude

                // Convert coordinates to address
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(currentLat, currentLng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressText = "${address.getAddressLine(0)}"
                        tvLocation.text = "📍 $addressText"
                    } else {
                        tvLocation.text = "📍 Lat: $currentLat, Lng: $currentLng"
                    }
                } catch (e: Exception) {
                    tvLocation.text = "📍 Lat: $currentLat, Lng: $currentLng"
                }
            } else {
                tvLocation.text = "📍 Could not get location"
            }
        }
    }

    private fun callNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private fun shareLocation() {
        if (currentLat == 0.0 && currentLng == 0.0) {
            Toast.makeText(this, "Getting location, please wait...", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "🚨 I need help! My location:\n" +
                "https://www.google.com/maps?q=$currentLat,$currentLng"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(intent, "Share location via"))
    }
}