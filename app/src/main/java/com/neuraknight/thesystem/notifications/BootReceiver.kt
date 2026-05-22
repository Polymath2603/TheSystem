package com.neuraknight.thesystem.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.neuraknight.thesystem.data.models.AppData

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("TheSystemApp", Context.MODE_PRIVATE)
        val json = prefs.getString("app_data", null)
        val appData = if (json != null) {
            try {
                Gson().fromJson(json, AppData::class.java)
            } catch (e: Exception) {
                // Backup corrupted JSON before returning null
                prefs.edit().putString("app_data_backup", json).apply()
                null
            }
        } else {
            null
        }

        if (appData != null && appData.setupComplete) {
            NotificationHelper.createChannels(context)
            NotificationScheduler.scheduleAll(context, appData.settings)
        }
    }
}
