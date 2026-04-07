package com.example.pulsemindai

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MedicationDBHelper(context: Context) :
    SQLiteOpenHelper(context, "MedicationDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE medications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                dosage TEXT,
                time TEXT,
                isEnabled INTEGER DEFAULT 1,
                userEmail TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS medications")
        onCreate(db)
    }

    fun addMedication(name: String, dosage: String, time: String, userEmail:String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("dosage", dosage)
            put("time", time)
            put("isEnabled", 1)
            put("userEmail", userEmail)
        }
        return db.insert("medications", null, values)
    }

    fun getAllMedications(userEmail: String): List<Medication> {
        val list = mutableListOf<Medication>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM medications where userEmail=?",arrayOf(userEmail) )
        while (cursor.moveToNext()) {
            list.add(Medication(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                dosage = cursor.getString(cursor.getColumnIndexOrThrow("dosage")),
                time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")) == 1
            ))
        }
        cursor.close()
        return list
    }

    fun deleteMedication(id: Int) {
        writableDatabase.delete("medications", "id=?", arrayOf(id.toString()))
    }

    fun updateEnabled(id: Int, isEnabled: Boolean) {
        val values = ContentValues().apply {
            put("isEnabled", if (isEnabled) 1 else 0)
        }
        writableDatabase.update("medications", values, "id=?", arrayOf(id.toString()))
    }
}