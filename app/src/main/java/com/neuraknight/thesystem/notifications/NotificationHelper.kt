package com.neuraknight.thesystem.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.neuraknight.thesystem.MainActivity
import com.neuraknight.thesystem.R

object NotificationHelper {
    const val CHANNEL_QUEST = "quest_reminders"
    const val CHANNEL_PRAYER = "prayer_alerts"
    const val CHANNEL_STREAK = "streak_warnings"
    const val CHANNEL_LEVEL_UP = "level_up"

    const val NOTIFICATION_WORKOUT_REMINDER = 1001
    const val NOTIFICATION_STREAK_WARNING = 1002
    const val NOTIFICATION_LEVEL_UP = 1003
    const val NOTIFICATION_PRAYER_BASE = 2000
    const val NOTIFICATION_QUEST_RESET = 1004

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(CHANNEL_QUEST, "Quest Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily workout quest reminders"
            },
            NotificationChannel(CHANNEL_PRAYER, "Prayer Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Prayer time notifications"
            },
            NotificationChannel(CHANNEL_STREAK, "Streak Warnings", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Streak at risk warnings"
            },
            NotificationChannel(CHANNEL_LEVEL_UP, "Level Up", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Level up achievements"
            }
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }

    private fun getMainActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showWorkoutReminder(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_QUEST)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("[SYSTEM] Daily Quest Awaits")
            .setContentText("Your training quest is ready. Don't break your streak!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getMainActivityIntent(context))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_WORKOUT_REMINDER, notification)
    }

    fun showPrayerAlert(context: Context, prayerName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("[SYSTEM] Prayer Time")
            .setContentText("$prayerName prayer time is approaching.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getMainActivityIntent(context))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_PRAYER_BASE + prayerName.hashCode() % 5, notification)
    }

    fun showStreakWarning(context: Context, streak: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("[SYSTEM] Streak at Risk!")
            .setContentText("Your $streak-day streak will reset if you don't complete today's quest.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getMainActivityIntent(context))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_STREAK_WARNING, notification)
    }

    fun showLevelUp(context: Context, level: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_LEVEL_UP)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("[SYSTEM] Level Up!")
            .setContentText("You have reached Level $level. Your power grows.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getMainActivityIntent(context))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_LEVEL_UP, notification)
    }

    fun showQuestExpired(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_QUEST)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("[SYSTEM] Quest Expired")
            .setContentText("You failed to complete your daily quest. A new quest awaits.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getMainActivityIntent(context))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_QUEST_RESET, notification)
    }
}
