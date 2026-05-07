package com.nxzef.wc.shared.util

object ErrorMessages {

    fun forLogin(raw: String?): String {
        val msg = raw.orEmpty()
        return when {
            msg.containsAny("401", "Unauthorized", "Invalid") -> "Invalid email or password"
            msg.containsAny("429", "Too many") -> "Too many attempts. Please wait a moment."
            msg.containsAny("404") -> "Account not found"
            else -> "Login failed. Please try again."
        }
    }

    fun forGeneric(raw: String?): String {
        val msg = raw.orEmpty()
        return when {
            msg.containsAny("401", "Unauthorized") -> "Session expired. Please sign in again."
            msg.containsAny("403", "Forbidden") -> "You don't have permission to do that."
            msg.containsAny("404", "Not Found") -> "Item not found."
            msg.containsAny("409", "Conflict") -> "That item already exists."
            msg.containsAny("429", "Too many") -> "Too many requests. Please wait."
            msg.containsAny("timeout", "Timeout", "TimeoutException") -> "Request timed out. Check your connection."
            msg.containsAny("UnknownHost", "ConnectException", "Failed to connect", "Network", "Unable to resolve") -> "No internet connection."
            msg.containsAny("500", "Internal Server", "502", "503", "504") -> "Server error. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }

    private fun String.containsAny(vararg needles: String): Boolean =
        needles.any { contains(it, ignoreCase = true) }
}
