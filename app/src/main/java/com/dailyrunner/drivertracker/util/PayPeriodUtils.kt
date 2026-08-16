package com.dailyrunner.drivertracker.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class PayPeriod(
    val startDate: LocalDate, // Friday
    val endDate: LocalDate,   // Thursday
    val weekId: String        // Format: YYYY-MM-DD_YYYY-MM-DD
) {
    val displayRange: String
        get() {
            val startFmt = startDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH))
            val endFmt = endDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH))
            return "$startFmt – $endFmt"
        }

    val shortRange: String
        get() {
            val startFmt = startDate.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
            val endFmt = endDate.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
            return "$startFmt – $endFmt"
        }
}

object PayPeriodUtils {

    val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTodayIso(): String = LocalDate.now().format(ISO_FORMATTER)

    fun getYesterdayIso(): String = LocalDate.now().minusDays(1).format(ISO_FORMATTER)

    /**
     * Given an ISO date string (YYYY-MM-DD) or LocalDate, compute the Friday-to-Thursday pay period.
     */
    fun getPayPeriodForDate(date: LocalDate): PayPeriod {
        // If the date is Friday, it's the start.
        // Otherwise, previous Friday is the start.
        val Friday = if (date.dayOfWeek == DayOfWeek.FRIDAY) {
            date
        } else {
            date.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY))
        }
        val Thursday = Friday.plusDays(6)
        val weekId = "${Friday.format(ISO_FORMATTER)}_${Thursday.format(ISO_FORMATTER)}"

        return PayPeriod(
            startDate = Friday,
            endDate = Thursday,
            weekId = weekId
        )
    }

    fun getPayPeriodForDateString(dateStr: String): PayPeriod {
        val date = try {
            LocalDate.parse(dateStr, ISO_FORMATTER)
        } catch (e: Exception) {
            LocalDate.now()
        }
        return getPayPeriodForDate(date)
    }

    fun getCurrentPayPeriod(): PayPeriod {
        return getPayPeriodForDate(LocalDate.now())
    }

    fun formatDateForDisplay(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, ISO_FORMATTER)
            date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH))
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, ISO_FORMATTER)
            date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatTimestamp(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return ""
        val javaDate = java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return javaDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
    }
}
