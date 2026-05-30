package com.neuraknight.thesystem.utils

import java.util.*
import kotlin.math.*

/**
 * SunCalc utility for astronomical calculations.
 * Derived from the standard solar position algorithms.
 */
object SunCalc {
    private const val RAD = PI / 180.0
    private const val DEG = 180.0 / PI

    data class SunTimes(
        val fajr: Date?,
        val sunrise: Date?,
        val dhuhr: Date?,
        val asr: Date?,
        val maghrib: Date?,
        val isha: Date?
    )

    enum class PrayerMethod {
        DEFAULT,      // MWL standard (18° Fajr, 17° Isha)
        MWL,          // Muslim World League (18° Fajr, 17° Isha)
        ISNA,         // Islamic Society of North America (15° Fajr, 15° Isha)
        EGYPTO,       // Egyptian General Authority (19.5° Fajr, 17.5° Isha)
        MAKKAH,       // Umm Al-Qura University (18.5° Fajr, Isha = 90 min after Maghrib)
        KARACHI,      // University of Islamic Sciences, Karachi (18° Fajr, 18° Isha)
        TEHRAN,       // Institute of Geophysics, University of Tehran (17.7° Fajr, 14° Isha)
        JAFARI        // Shia Ithna-Ashari (16° Fajr, 14° Isha)
    }

    private fun getFajrAngle(method: PrayerMethod): Double = when (method) {
        PrayerMethod.DEFAULT -> -18.0
        PrayerMethod.MWL -> -18.0
        PrayerMethod.ISNA -> -15.0
        PrayerMethod.EGYPTO -> -19.5
        PrayerMethod.MAKKAH -> -18.5
        PrayerMethod.KARACHI -> -18.0
        PrayerMethod.TEHRAN -> -17.7
        PrayerMethod.JAFARI -> -16.0
    }

    private fun getIshaAngleOrNull(method: PrayerMethod): Double? = when (method) {
        PrayerMethod.DEFAULT -> -17.0
        PrayerMethod.MWL -> -17.0
        PrayerMethod.ISNA -> -15.0
        PrayerMethod.EGYPTO -> -17.5
        PrayerMethod.MAKKAH -> null // Umm Al-Qura uses fixed 90 min after Maghrib
        PrayerMethod.KARACHI -> -18.0
        PrayerMethod.TEHRAN -> -14.0
        PrayerMethod.JAFARI -> -14.0
    }

    fun getPrayerTimes(date: Calendar, lat: Double, lng: Double, method: PrayerMethod = PrayerMethod.DEFAULT, altitude: Double = 0.0): SunTimes {
        val dayOfYear = date.get(Calendar.DAY_OF_YEAR).toDouble()

        // Solar noon and sun position in UTC hours
        val solarNoon = getSolarNoon(dayOfYear, lng)
        val declination = getSolarDeclination(dayOfYear)

        val fajrAngle = getFajrAngle(method)
        val ishaAngle = getIshaAngleOrNull(method)

        val sunriseTime = getTimeByAngle(solarNoon, lat, declination, -0.833, true)
        val maghribTime = getTimeByAngle(solarNoon, lat, declination, -0.833, false)

        var fajrTime = getTimeByAngle(solarNoon, lat, declination, fajrAngle, true)

        // Isha: MAKKAH method uses fixed 90 min after Maghrib; others use angle
        var ishaTime: Double? = if (method == PrayerMethod.MAKKAH && maghribTime != null) {
            (maghribTime + 1.5).coerceAtMost(24.0) // 90 minutes = 1.5 hours
        } else if (ishaAngle != null) {
            getTimeByAngle(solarNoon, lat, declination, ishaAngle, false)
        } else {
            null
        }

        // Fallback for high latitudes where standard angles never occur (e.g. summer above ~48°N)
        if (fajrTime == null && sunriseTime != null && maghribTime != null) {
            val nightDuration = (24.0 - maghribTime) + sunriseTime
            fajrTime = (sunriseTime - nightDuration / 3.0).coerceAtLeast(0.0)
        }
        if (ishaTime == null && maghribTime != null && sunriseTime != null) {
            val nightDuration = (24.0 - maghribTime) + sunriseTime
            ishaTime = (maghribTime + nightDuration / 3.0).coerceAtMost(24.0)
        }

        // Asr calculation (Shafi'i: shadow factor = 1)
        val asrTime = getAsrTime(solarNoon, lat, declination, 1.0)

        // Create Date objects from UTC hours — SimpleDateFormat in the app
        // will convert these to the device's local timezone for display
        return SunTimes(
            fajr = dateFromHoursUTC(date, fajrTime),
            sunrise = dateFromHoursUTC(date, sunriseTime),
            dhuhr = dateFromHoursUTC(date, solarNoon),
            asr = dateFromHoursUTC(date, asrTime),
            maghrib = dateFromHoursUTC(date, maghribTime),
            isha = dateFromHoursUTC(date, ishaTime)
        )
    }

    private fun getSolarNoon(day: Double, lng: Double): Double {
        // Solar noon in UTC fractional hours
        val eqt = getEquationOfTime(day)
        return 12.0 - (lng / 15.0) - (eqt / 60.0)
    }

    private fun getEquationOfTime(day: Double): Double {
        val b = 360.0 / 365.0 * (day - 81.0) * RAD
        return 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)
    }

    private fun getSolarDeclination(day: Double): Double {
        return 23.44 * sin(360.0 / 365.0 * (day - 81.0) * RAD)
    }

    private fun getTimeByAngle(noon: Double, lat: Double, decl: Double, angle: Double, isMorning: Boolean): Double? {
        val latR = lat * RAD
        val declR = decl * RAD
        val angR = angle * RAD
        
        val cosH = (sin(angR) - sin(latR) * sin(declR)) / (cos(latR) * cos(declR))
        if (cosH > 1.0 || cosH < -1.0) return null
        
        val h = acos(cosH) * DEG / 15.0
        return if (isMorning) noon - h else noon + h
    }

    private fun getAsrTime(noon: Double, lat: Double, decl: Double, factor: Double): Double? {
        val latR = lat * RAD
        val declR = decl * RAD
        val angle = factor + tan(abs(latR - declR))
        val asrAngle = atan(1.0 / angle) * DEG
        
        return getTimeByAngle(noon, lat, decl, asrAngle, false)
    }

    private fun dateFromHoursUTC(base: Calendar, utcHours: Double?): Date? {
        if (utcHours == null) return null
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, base.get(Calendar.YEAR))
            set(Calendar.MONTH, base.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, base.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis += (utcHours * 3_600_000.0).toLong()
        }
        return cal.time
    }
}
