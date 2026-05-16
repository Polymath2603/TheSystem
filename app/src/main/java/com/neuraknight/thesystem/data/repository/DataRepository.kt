package com.neuraknight.thesystem.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.neuraknight.thesystem.data.models.AppData

class DataRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("TheSystemApp", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadData(): AppData {
        val json = sharedPreferences.getString("app_data", null)
        return if (json != null) {
            try {
                gson.fromJson(json, AppData::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e("DataRepository", "Failed to parse saved data - JSON is corrupted", e)
                // Backup the corrupted JSON before returning defaults
                sharedPreferences.edit().putString("app_data_backup", json).apply()
                AppData()
            } catch (e: Exception) {
                Log.e("DataRepository", "Unexpected error loading data", e)
                sharedPreferences.edit().putString("app_data_backup", json).apply()
                AppData()
            }
        } else {
            AppData()
        }
    }

    fun saveData(data: AppData) {
        val json = gson.toJson(data)
        sharedPreferences.edit().putString("app_data", json).apply()
    }
}
