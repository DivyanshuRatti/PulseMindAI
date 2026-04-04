package com.example.pulsemindai

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MedicationDBHelper(context: Context) :
    SQLiteOpenHelper(context, "MedicationDB", null, 1) {

    // Called when database is created for first time
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE medications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                dosage TEXT,
                time TEXT,
                isEnabled INTEGER DEFAULT 1
            )
        """)
    }

    // Called when you upgrade database version
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS medications")
        onCreate(db)
    }

    // ADD a new medication
    fun addMedication(name: String, dosage: String, time: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("dosage", dosage)
            put("time", time)
            put("isEnabled", 1)
        }
        return db.insert("medications", null, values)
    }

    // GET all medications
    fun getAllMedications(): List<Medication> {
        val list = mutableListOf<Medication>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM medications", null)

        while (cursor.moveToNext()) {
            val med = Medication(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                dosage = cursor.getString(cursor.getColumnIndexOrThrow("dosage")),
                time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")) == 1
            )
            list.add(med)
        }
        cursor.close()
        return list
    }

    // DELETE a medication
    fun deleteMedication(id: Int) {
        val db = writableDatabase
        db.delete("medications", "id=?", arrayOf(id.toString()))
    }

    // UPDATE enabled/disabled toggle
    fun updateEnabled(id: Int, isEnabled: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("isEnabled", if (isEnabled) 1 else 0)
        }
        db.update("medications", values, "id=?", arrayOf(id.toString()))
    }
}