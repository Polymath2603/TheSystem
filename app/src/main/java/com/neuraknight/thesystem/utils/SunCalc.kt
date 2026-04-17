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
        DEFAULT,      // Standard -18 degrees
        MWL,          // Muslim World League -18 degrees
        ISNA,         // Islamic Society of North America -15 degrees
        EGYPTO,       // Egyptian General Authority -19.5 degrees
        MAKKAH,       // Umm Al-Qura University -18.5 degrees
        KARACHI       // Karachi -18 degrees
    }

    private fun getFajrAngle(method: PrayerMethod): Double = when (method) {
        PrayerMethod.DEFAULT -> -18.0
        PrayerMethod.MWL -> -18.0
        PrayerMethod.ISNA -> -15.0
        PrayerMethod.EGYPTO -> -19.5
        PrayerMethod.MAKKAH -> -18.5
        PrayerMethod.KARACHI -> -18.0
    }

    private fun getIshaAngle(method: PrayerMethod): Double = when (method) {
        PrayerMethod.DEFAULT -> -18.0
        PrayerMethod.MWL -> -17.0
        PrayerMethod.ISNA -> -15.0
        PrayerMethod.EGYPTO -> -17.5
        PrayerMethod.MAKKAH -> -18.5
        PrayerMethod.KARACHI -> -18.0
    }

    fun getPrayerTimes(date: Calendar, lat: Double, lng: Double, method: PrayerMethod = PrayerMethod.DEFAULT, altitude: Double = 0.0): SunTimes {
        val dayOfYear = date.get(Calendar.DAY_OF_YEAR).toDouble()
        
        // Simplified Solar Position Math
        val solarNoon = getSolarNoon(dayOfYear, lng, date)
        val declination = getSolarDeclination(dayOfYear)
        
        val fajrAngle = getFajrAngle(method)
        val ishaAngle = getIshaAngle(method)
        
        val fajrTime = getTimeByAngle(solarNoon, lat, declination, fajrAngle, true)
        val sunriseTime = getTimeByAngle(solarNoon, lat, declination, -0.833, true)
        val maghribTime = getTimeByAngle(solarNoon, lat, declination, -0.833, false)
        val ishaTime = getTimeByAngle(solarNoon, lat, declination, ishaAngle, false)
        
        // Asr calculation (Shafi'i: shadow factor = 1)
        val asrTime = getAsrTime(solarNoon, lat, declination, 1.0)

        return SunTimes(
            fajr = dateFromHours(date, fajrTime),
            sunrise = dateFromHours(date, sunriseTime),
            dhuhr = dateFromHours(date, solarNoon),
            asr = dateFromHours(date, asrTime),
            maghrib = dateFromHours(date, maghribTime),
            isha = dateFromHours(date, ishaTime)
        )
    }

    private fun getSolarNoon(day: Double, lng: Double, date: Calendar): Double {
        val timezone = date.timeZone.rawOffset / 3600000.0
        val eqt = getEquationOfTime(day)
        return 12.0 + timezone - (lng / 15.0) - (eqt / 60.0)
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

    private fun dateFromHours(base: Calendar, hours: Double?): Date? {
        if (hours == null) return null
        val cal = base.clone() as Calendar
        val h = floor(hours).toInt()
        val m = floor((hours - h) * 60.0).toInt()
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        return cal.time
    }
}
