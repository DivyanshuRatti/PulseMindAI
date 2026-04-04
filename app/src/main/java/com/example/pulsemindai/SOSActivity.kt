package com.example.pulsemindai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class SOSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSOSLarge = findViewById<ConstraintLayout>(R.id.btnSOSLarge)
        val pulseView = findViewById<View>(R.id.pulseView)

        btnBack.setOnClickListener {
            finish()
        }

        btnSOSLarge.setOnClickListener {
            // Start Pulse Animation
            val animation = AnimationUtils.loadAnimation(this, R.anim.pulse)
            pulseView.visibility = View.VISIBLE
            pulseView.startAnimation(animation)

            // Initiate Call immediately
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:911")
            startActivity(intent)
            
            Toast.makeText(this, "Initiating Emergency Call...", Toast.LENGTH_SHORT).show()
        }
    }
}
