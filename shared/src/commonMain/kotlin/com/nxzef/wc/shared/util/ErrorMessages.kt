package com.nxzef.wc.shared.util

object ErrorMessages {

    fun forLogin(raw: String?): String {
        val msg = raw.orEmpty()
        return when {
            msg.containsAny("invite code") -> "Please use Join Team with your invite code."
            msg.containsAny("already joined") -> "Already joined. Please sign in with your email and password."
            msg.containsAny("Invalid", "401") -> "Invalid email or password."
            msg.containsAny("not found", "404") -> "Account not found."
            msg.containsAny("429", "Too many") -> "Too many attempts. Please wait a moment."
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

    fun extractServerMessage(raw: String?): String {
        if (raw == null) return "Something went wrong."
        // Try to find "message":"..." pattern
        val match = Regex(""""message"\s*:\s*"([^"]+)"""")
            .find(raw)
        return match?.groupValues?.get(1) ?: raw.take(100)
    }

    private fun String.containsAny(vararg needles: String): Boolean =
        needles.any { contains(it, ignoreCase = true) }
}
