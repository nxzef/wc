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
            val month = monthAbbr(date.month.ordinal + 1)
            "${date.day} $month ${date.year}"
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatShortDate(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate)
            "${date.day} ${monthAbbr(date.month.ordinal + 1)}"
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
        return "${monthAbbr(today.month.ordinal + 1)} ${today.year}"
    }

    fun getCurrentYear(): Int =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    fun getCurrentMonth(): Int =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).month.ordinal + 1

    // Kept for backward compatibility
    fun formatIsoDate(isoDate: String): String = isoDate

    private fun monthAbbr(month: Int): String = when (month) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> ""
    }
}
