package com.neuraknight.thesystem.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.neuraknight.thesystem.data.models.AppData

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_PRAYER_INDEX = "prayer_index"
        const val EXTRA_PRAYER_NAME = "prayer_name"

        const val TYPE_WORKOUT = "workout"
        const val TYPE_PRAYER = "prayer"
        const val TYPE_STREAK = "streak"
        const val TYPE_QUEST_RESET = "quest_reset"
        const val TYPE_PRAYER_REFRESH = "prayer_refresh"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val appData = loadAppData(context)

        when (type) {
            TYPE_WORKOUT -> {
                if (appData.settings.notificationsEnabled && appData.settings.workoutReminderEnabled) {
                    if (!appData.quest.completed) {
                        NotificationHelper.showWorkoutReminder(context)
                    }
                }
                NotificationScheduler.scheduleWorkoutReminder(context, appData.settings)
            }
            TYPE_PRAYER -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
                if (appData.settings.notificationsEnabled && appData.settings.prayerNotificationsEnabled) {
                    NotificationHelper.showPrayerAlert(context, prayerName)
                }
            }
            TYPE_STREAK -> {
                if (appData.settings.notificationsEnabled && appData.settings.streakWarningEnabled) {
                    if (!appData.quest.completed && appData.user.streak > 0) {
                        NotificationHelper.showStreakWarning(context, appData.user.streak)
                    }
                }
                NotificationScheduler.scheduleStreakWarning(context, appData.settings)
            }
            TYPE_QUEST_RESET -> {
                val prefs = context.getSharedPreferences("TheSystemApp", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("quest_reset_pending", true).apply()
                if (!appData.quest.completed) {
                    NotificationHelper.showQuestExpired(context)
                }
                NotificationScheduler.scheduleQuestReset(context)
            }
            TYPE_PRAYER_REFRESH -> {
                if (appData.settings.notificationsEnabled && appData.settings.prayerNotificationsEnabled) {
                    NotificationScheduler.schedulePrayerNotifications(context, appData.settings)
                }
            }
        }
    }

    private fun loadAppData(context: Context): AppData {
        val prefs = context.getSharedPreferences("TheSystemApp", Context.MODE_PRIVATE)
        val json = prefs.getString("app_data", null)
        return if (json != null) {
            try {
                Gson().fromJson(json, AppData::class.java)
            } catch (e: Exception) {
                AppData()
            }
        } else {
            AppData()
        }
    }
}
