package com.example.pulsemindai

data class HealthData(
    val id: Int,
    val date: String,
    val steps: Int,
    val water: Int,
    val sleep: Double,
    val calories: Int,
    val weight: Double,
    val heartRate: Int
)