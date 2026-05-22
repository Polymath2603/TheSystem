package com.neuraknight.thesystem.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.neuraknight.thesystem.data.models.Settings
import com.neuraknight.thesystem.utils.SunCalc
import java.util.Calendar

object NotificationScheduler {
    private const val REQUEST_WORKOUT = 3001
    private const val REQUEST_STREAK = 3002
    private const val REQUEST_QUEST_RESET = 3003
    private const val REQUEST_PRAYER_BASE = 3100
    private const val REQUEST_PRAYER_REFRESH = 3200

    fun scheduleAll(context: Context, settings: Settings) {
        if (!settings.notificationsEnabled) {
            cancelAll(context)
            return
        }
        if (settings.workoutReminderEnabled) scheduleWorkoutReminder(context, settings)
        else cancelWorkoutReminder(context)

        if (settings.streakWarningEnabled) scheduleStreakWarning(context, settings)
        else cancelStreakWarning(context)

        if (settings.prayerNotificationsEnabled) schedulePrayerNotifications(context, settings)
        else cancelAllPrayerAlarms(context)

        scheduleQuestReset(context)
    }

    fun cancelAll(context: Context) {
        cancelWorkoutReminder(context)
        cancelStreakWarning(context)
        cancelAllPrayerAlarms(context)
        cancelQuestReset(context)
        cancelPrayerRefresh(context)
    }

    fun scheduleWorkoutReminder(context: Context, settings: Settings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_WORKOUT)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_WORKOUT, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.workoutReminderHour)
            set(Calendar.MINUTE, settings.workoutReminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        }
    }

    private fun cancelWorkoutReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_WORKOUT, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun schedulePrayerNotifications(context: Context, settings: Settings) {
        cancelAllPrayerAlarms(context)

        val cal = Calendar.getInstance()
        val method = when (settings.prayerAlgorithm) {
            "mwl" -> SunCalc.PrayerMethod.MWL
            "isna" -> SunCalc.PrayerMethod.ISNA
            "egypto" -> SunCalc.PrayerMethod.EGYPTO
            "makkah" -> SunCalc.PrayerMethod.MAKKAH
            "karachi" -> SunCalc.PrayerMethod.KARACHI
            "tehran" -> SunCalc.PrayerMethod.TEHRAN
            "jafari" -> SunCalc.PrayerMethod.JAFARI
            else -> SunCalc.PrayerMethod.DEFAULT
        }
        val sunTimes = SunCalc.getPrayerTimes(cal, settings.prayerLatitude, settings.prayerLongitude, method)

        val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val prayerDates = listOf(sunTimes.fajr, sunTimes.dhuhr, sunTimes.asr, sunTimes.maghrib, sunTimes.isha)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val leadMs = settings.prayerNotificationLeadMinutes * 60 * 1000L

        prayerNames.forEachIndexed { index, name ->
            val prayerDate = prayerDates[index] ?: return@forEachIndexed
            val triggerTime = prayerDate.time - leadMs

            if (triggerTime > System.currentTimeMillis()) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_PRAYER)
                    putExtra(AlarmReceiver.EXTRA_PRAYER_INDEX, index)
                    putExtra(AlarmReceiver.EXTRA_PRAYER_NAME, name)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, REQUEST_PRAYER_BASE + index, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (canScheduleExactAlarms(context)) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                }
            }
        }

        scheduleNextDayPrayerRefresh(context)
    }

    private fun cancelAllPrayerAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..4) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_PRAYER_BASE + i, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun scheduleNextDayPrayerRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_PRAYER_REFRESH)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_PRAYER_REFRESH, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent
            )
        }
    }

    private fun cancelPrayerRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_PRAYER_REFRESH, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun scheduleStreakWarning(context: Context, settings: Settings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_STREAK)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_STREAK, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.streakWarningHour)
            set(Calendar.MINUTE, settings.streakWarningMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        }
    }

    private fun cancelStreakWarning(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_STREAK, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun scheduleQuestReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_QUEST_RESET)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_QUEST_RESET, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent
            )
        }
    }

    private fun cancelQuestReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_QUEST_RESET, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }
}
