package com.nxzef.wc.shared.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

object DateUtils {
    fun getCurrentDateIso(): String {
        return Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    }

    fun formatIsoDate(isoDate: String): String {
        // Basic implementation, can be expanded for more complex formatting
        return isoDate
    }
}