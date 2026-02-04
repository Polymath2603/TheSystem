package com.neuraknight.thesystem.data.repository

import android.content.Context
import com.google.gson.Gson
import com.neuraknight.thesystem.data.models.AppData

class DataRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("TheSystemApp", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadData(): AppData {
        val json = sharedPreferences.getString("app_data", null)
        return if (json != null) {
            try {
                gson.fromJson(json, AppData::class.java)
            } catch (e: Exception) {
                // If parsing fails, return default data
                AppData()
            }
        } else {
            AppData() // Return default data if nothing is saved
        }
    }

    fun saveData(data: AppData) {
        val json = gson.toJson(data)
        sharedPreferences.edit().putString("app_data", json).apply()
    }
}
