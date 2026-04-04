package com.example.pulsemindai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.pulsemindai.BuildConfig

class NearbyClinicsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clinicsContainer: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST = 1001
    private val PLACES_API_KEY = BuildConfig.GOOGLE_MAPS_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_clinics)

        clinicsContainer = findViewById(R.id.clinicsContainer)
        tvLoading = findViewById(R.id.tvLoading)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission is needed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        tvLoading.visibility = View.VISIBLE
        googleMap.isMyLocationEnabled = true

        // High accuracy location request
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1  // only need one update
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Got real location!
                val location: Location = locationResult.lastLocation ?: return

                val userLatLng = LatLng(location.latitude, location.longitude)

                // Move camera to REAL location
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                )

                // Blue dot for user location
                googleMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("📍 You are here")
                )

                // Stop location updates - we have what we need
                fusedLocationClient.removeLocationUpdates(locationCallback)

                // Search clinics at this real location
                searchNearbyClinics(location.latitude, location.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun searchNearbyClinics(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=$lat,$lng" +
                        "&radius=20000" +
                        "&type=hospital" +
                        "&key=$PLACES_API_KEY"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                val response = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    val error = connection.errorStream.bufferedReader().readText()
                    throw Exception("Places API Error $responseCode: $error")
                }

                connection.disconnect()

                val json = JSONObject(response)
                val status = json.getString("status")

                // Handle Places API status
                if (status != "OK" && status != "ZERO_RESULTS") {
                    throw Exception("Places API status: $status")
                }

                val results = json.getJSONArray("results")

                withContext(Dispatchers.Main) {
                    tvLoading.visibility = View.GONE
                    clinicsContainer.removeAllViews()

                    if (results.length() == 0) {
                        val tvEmpty = TextView(this@NearbyClinicsActivity)
                        tvEmpty.text = "No clinics found within 3km"
                        tvEmpty.textSize = 14f
                        tvEmpty.setPadding(16, 16, 16, 16)
                        clinicsContainer.addView(tvEmpty)
                        return@withContext
                    }

                    for (i in 0 until minOf(results.length(), 5)) {
                        val place = results.getJSONObject(i)
                        val name = place.getString("name")
                        val address = place.optString("vicinity", "Address not available")
                        val rating = place.optDouble("rating", 0.0)
                        val isOpen = place
                            .optJSONObject("opening_hours")
                            ?.optBoolean("open_now", false) ?: false
                        val placeLat = place
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .getDouble("lat")
                        val placeLng = place
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .getDouble("lng")

                        // Pin on map
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(placeLat, placeLng))
                                .title(name)
                        )

                        // Card in list
                        addClinicCard(name, address, rating, isOpen, placeLat, placeLng)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvLoading.visibility = View.GONE
                    Toast.makeText(
                        this@NearbyClinicsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun addClinicCard(
        name: String,
        address: String,
        rating: Double,
        isOpen: Boolean,
        lat: Double,
        lng: Double
    ) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.WHITE)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16)
        card.layoutParams = params
        card.setPadding(20, 20, 20, 20)

        // Name
        val tvName = TextView(this)
        tvName.text = "🏥 $name"
        tvName.textSize = 15f
        tvName.setTextColor(Color.parseColor("#0A3D62"))
        tvName.paint.isFakeBoldText = true
        card.addView(tvName)

        // Address
        val tvAddress = TextView(this)
        tvAddress.text = "📍 $address"
        tvAddress.textSize = 12f
        tvAddress.setTextColor(Color.parseColor("#666666"))
        tvAddress.setPadding(0, 6, 0, 0)
        card.addView(tvAddress)

        // Rating + Open row
        val rowInfo = LinearLayout(this)
        rowInfo.orientation = LinearLayout.HORIZONTAL
        rowInfo.setPadding(0, 8, 0, 8)

        val tvRating = TextView(this)
        tvRating.text = if (rating > 0) "⭐ $rating" else "⭐ No rating"
        tvRating.textSize = 12f
        tvRating.setTextColor(Color.parseColor("#F39C12"))
        rowInfo.addView(tvRating)

        val tvOpen = TextView(this)
        tvOpen.text = if (isOpen) "   🟢 Open Now" else "   🔴 Closed"
        tvOpen.textSize = 12f
        tvOpen.setTextColor(
            if (isOpen) Color.parseColor("#27AE60")
            else Color.parseColor("#E74C3C")
        )
        rowInfo.addView(tvOpen)
        card.addView(rowInfo)

        // Directions button
        val btnDirections = Button(this)
        btnDirections.text = "🗺️ Get Directions"
        btnDirections.textSize = 13f
        btnDirections.setTextColor(Color.WHITE)
        btnDirections.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor("#00B5A5"))
        btnDirections.setOnClickListener {
            val uri = Uri.parse("google.navigation:q=$lat,$lng")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val browserUri =
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }
        card.addView(btnDirections)

        clinicsContainer.addView(card)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when leaving page
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}