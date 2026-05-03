package com.nxzef.wc.shared.util

object CurrencyUtils {

    fun formatINR(amount: Double): String {
        val long = amount.toLong()
        return "₹" + formatIndianNumber(long.toString())
    }

    private fun formatIndianNumber(num: String): String {
        if (num.length <= 3) return num
        val lastThree = num.takeLast(3)
        val remaining = num.dropLast(3)
        val formatted = remaining.reversed().chunked(2).joinToString(",").reversed()
        return "$formatted,$lastThree"
    }

    fun formatINRShort(amount: Double): String {
        return when {
            amount >= 100000 -> {
                val v = amount / 100000.0
                val integer = v.toInt()
                val decimal = ((v - integer) * 10).toInt()
                val str = if (decimal == 0) "$integer" else "$integer.$decimal"
                "₹${str}L"
            }
            amount >= 1000 -> {
                val v = amount / 1000.0
                val integer = v.toInt()
                val decimal = ((v - integer) * 10).toInt()
                val str = if (decimal == 0) "$integer" else "$integer.$decimal"
                "₹${str}K"
            }
            else -> formatINR(amount)
        }
    }
}
