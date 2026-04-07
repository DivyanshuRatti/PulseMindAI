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

class NearbyClinicsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clinicsContainer: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST = 1001
    private val MAPS_API_KEY = BuildConfig.MAPS_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_clinics)

        clinicsContainer = findViewById(R.id.clinicsContainer)
        tvLoading        = findViewById(R.id.tvLoading)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

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
                this, Manifest.permission.ACCESS_FINE_LOCATION
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
            Toast.makeText(this, "Location permission is needed to find clinics", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        tvLoading.visibility = View.VISIBLE
        googleMap.isMyLocationEnabled = true

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdates(1)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location = locationResult.lastLocation ?: return
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
                googleMap.addMarker(MarkerOptions().position(userLatLng).title("You are here"))
                fusedLocationClient.removeLocationUpdates(locationCallback)
                searchNearbyClinics(location.latitude, location.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun searchNearbyClinics(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=$lat,$lng&radius=5000&type=hospital&key=$MAPS_API_KEY"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = if (connection.responseCode == 200)
                    connection.inputStream.bufferedReader().readText()
                else throw Exception("Places API Error ${connection.responseCode}")
                connection.disconnect()

                val results = JSONObject(response).getJSONArray("results")

                withContext(Dispatchers.Main) {
                    tvLoading.visibility = View.GONE
                    clinicsContainer.removeAllViews()

                    if (results.length() == 0) {
                        val tvEmpty = TextView(this@NearbyClinicsActivity)
                        tvEmpty.text = "No clinics found nearby"
                        tvEmpty.textSize = 14f
                        tvEmpty.setPadding(16, 16, 16, 16)
                        clinicsContainer.addView(tvEmpty)
                        return@withContext
                    }

                    for (i in 0 until minOf(results.length(), 5)) {
                        val place    = results.getJSONObject(i)
                        val name     = place.getString("name")
                        val address  = place.optString("vicinity", "Address not available")
                        val rating   = place.optDouble("rating", 0.0)
                        val isOpen   = place.optJSONObject("opening_hours")
                            ?.optBoolean("open_now", false) ?: false
                        val placeLat = place.getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lat")
                        val placeLng = place.getJSONObject("geometry")
                            .getJSONObject("location").getDouble("lng")

                        googleMap.addMarker(
                            MarkerOptions().position(LatLng(placeLat, placeLng)).title(name)
                        )
                        addClinicCard(name, address, rating, isOpen, placeLat, placeLng)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvLoading.visibility = View.GONE
                    Toast.makeText(
                        this@NearbyClinicsActivity,
                        "Could not load clinics: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun addClinicCard(
        name: String, address: String, rating: Double,
        isOpen: Boolean, lat: Double, lng: Double
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 0, 0, 16) }
            setPadding(20, 20, 20, 20)
        }

        card.addView(TextView(this).apply {
            text = name; textSize = 15f
            setTextColor(Color.parseColor("#0A3D62"))
            paint.isFakeBoldText = true
        })
        card.addView(TextView(this).apply {
            text = address; textSize = 12f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 6, 0, 0)
        })

        val rowInfo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }
        rowInfo.addView(TextView(this).apply {
            text = if (rating > 0) "Rating: $rating" else "No rating"
            textSize = 12f; setTextColor(Color.parseColor("#F39C12"))
        })
        rowInfo.addView(TextView(this).apply {
            text = if (isOpen) "   Open Now" else "   Closed"
            textSize = 12f
            setTextColor(if (isOpen) Color.parseColor("#27AE60") else Color.parseColor("#E74C3C"))
        })
        card.addView(rowInfo)

        card.addView(Button(this).apply {
            text = "Get Directions"; textSize = 13f
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#00B5A5"))
            setOnClickListener {
                val uri = Uri.parse("google.navigation:q=$lat,$lng")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (mapIntent.resolveActivity(packageManager) != null) startActivity(mapIntent)
                else startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")))
            }
        })
        clinicsContainer.addView(card)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}