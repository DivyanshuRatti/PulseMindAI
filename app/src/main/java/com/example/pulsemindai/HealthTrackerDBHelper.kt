package com.example.pulsemindai

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HealthTrackerDBHelper(context: Context) :
    SQLiteOpenHelper(context, "HealthTrackerDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE health_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                steps INTEGER DEFAULT 0,
                water INTEGER DEFAULT 0,
                sleep REAL DEFAULT 0,
                calories INTEGER DEFAULT 0,
                weight REAL DEFAULT 0,
                heart_rate INTEGER DEFAULT 0
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS health_data")
        onCreate(db)
    }

    // Save today's data - if today exists update it, else insert new
    fun saveTodayData(
        date: String,
        steps: Int,
        water: Int,
        sleep: Double,
        calories: Int,
        weight: Double,
        heartRate: Int
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("date", date)
            put("steps", steps)
            put("water", water)
            put("sleep", sleep)
            put("calories", calories)
            put("weight", weight)
            put("heart_rate", heartRate)
        }

        // Check if today's record already exists
        val cursor = db.rawQuery(
            "SELECT id FROM health_data WHERE date=?",
            arrayOf(date)
        )

        if (cursor.moveToFirst()) {
            // Update existing record
            val id = cursor.getInt(0)
            db.update("health_data", values, "id=?", arrayOf(id.toString()))
        } else {
            // Insert new record
            db.insert("health_data", null, values)
        }
        cursor.close()
    }

    // Get today's data
    fun getTodayData(date: String): HealthData? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM health_data WHERE date=?",
            arrayOf(date)
        )

        return if (cursor.moveToFirst()) {
            val data = HealthData(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps")),
                water = cursor.getInt(cursor.getColumnIndexOrThrow("water")),
                sleep = cursor.getDouble(cursor.getColumnIndexOrThrow("sleep")),
                calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories")),
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
                heartRate = cursor.getInt(cursor.getColumnIndexOrThrow("heart_rate"))
            )
            cursor.close()
            data
        } else {
            cursor.close()
            null
        }
    }
}