package com.nxzef.wc.util

/**
 * Filters a list by a text query against one or more string extractors.
 * Returns the original list unchanged when the query is blank.
 */
fun <T> List<T>.applySearch(query: String, vararg fields: (T) -> String?): List<T> {
    if (query.isBlank()) return this
    val q = query.trim()
    return filter { item ->
        fields.any { extract -> extract(item)?.contains(q, ignoreCase = true) == true }
    }
}
