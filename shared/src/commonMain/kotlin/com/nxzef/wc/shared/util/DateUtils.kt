package com.nxzef.wc.shared.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

object DateUtils {

    fun getCurrentDateIso(): String =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate)
            val month = getMonthName(date.month.ordinal + 1)
            "${date.day} $month ${date.year}"
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatShortDate(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate)
            "${date.day} ${getMonthName(date.month.ordinal + 1)}"
        } catch (_: Exception) {
            isoDate
        }
    }

    fun isToday(isoDate: String): Boolean {
        return try {
            val date = LocalDate.parse(isoDate)
            date == Clock.System.todayIn(TimeZone.currentSystemDefault())
        } catch (_: Exception) {
            false
        }
    }

    fun isPast(isoDate: String): Boolean {
        return try {
            val date = LocalDate.parse(isoDate)
            date < Clock.System.todayIn(TimeZone.currentSystemDefault())
        } catch (_: Exception) {
            false
        }
    }

    fun daysBetween(from: String, to: String): Int {
        return try {
            val fromDate = LocalDate.parse(from)
            val toDate = LocalDate.parse(to)
            (toDate.toEpochDays() - fromDate.toEpochDays()).toInt()
        } catch (_: Exception) {
            0
        }
    }

    fun getCurrentMonthYear(): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return "${getMonthName(today.month.ordinal + 1)} ${today.year}"
    }

    fun getCurrentYear(): Int =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    fun getCurrentMonth(): Int =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).month.ordinal + 1

    // Kept for backward compatibility
    fun formatIsoDate(isoDate: String): String = isoDate

    fun getYear(isoDate: String?): Int? {
        if (isoDate == null) return null
        return try {
            val date = LocalDate.parse(isoDate)
            date.year
        } catch (_: Exception) {
            val parts = isoDate.split("-")
            if (parts.isNotEmpty()) parts[0].toIntOrNull() else null
        }
    }

    fun getMonth(isoDate: String?): Int? {
        if (isoDate == null) return null
        return try {
            val date = LocalDate.parse(isoDate)
            date.month.ordinal + 1
        } catch (_: Exception) {
            val parts = isoDate.split("-")
            if (parts.size >= 2) parts[1].toIntOrNull() else null
        }
    }

    fun getMonthName(month: Int): String = when (month) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> ""
    }

    fun formatDateRange(startIso: String, endIso: String?): String {
        if (endIso.isNullOrBlank()) return formatDisplayDate(startIso)
        return try {
            val start = LocalDate.parse(startIso)
            val end = LocalDate.parse(endIso)
            if (start == end) return formatDisplayDate(startIso)
            val sm = getMonthName(start.month.ordinal + 1)
            val em = getMonthName(end.month.ordinal + 1)
            when {
                start.year == end.year && start.month == end.month ->
                    "${start.day}–${end.day} $sm ${end.year}"
                start.year == end.year ->
                    "${start.day} $sm – ${end.day} $em ${end.year}"
                else ->
                    "${start.day} $sm ${start.year} – ${end.day} $em ${end.year}"
            }
        } catch (_: Exception) {
            formatDisplayDate(startIso)
        }
    }
}
