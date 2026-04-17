package com.example.pulsemindai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var taglineTextView: TextView
    private val fullText = "Because your health matters"
    private var index = 0

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taglineTextView = findViewById(R.id.tagline)

        startTypewriterEffect()
    }

    private fun startTypewriterEffect() {
        val runnable = object : Runnable {
            override fun run() {
                if (index < fullText.length) {
                    taglineTextView.text = fullText.substring(0, index + 1)
                    index++
                    handler.postDelayed(this, 80)
                } else {
                    handler.postDelayed({
                        startActivity(Intent(this@MainActivity, LoginPage::class.java))
                        finish()
                    }, 1000)
                }
            }
        }

        handler.postDelayed(runnable, 700)
    }
}
