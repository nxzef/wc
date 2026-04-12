package com.nxzef.wc.presentation.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import kotlinx.serialization.json.Json

inline fun <reified T : Any> serializableNavType() = object : NavType<T>(false) {
    override fun put(bundle: SavedState, key: String, value: T) {
        StringType.put(bundle, key, Json.encodeToString(value))
    }

    override fun get(bundle: SavedState, key: String): T? {
        return StringType[bundle, key]?.let { Json.decodeFromString(it) }
    }

    override fun parseValue(value: String): T = Json.decodeFromString(value)
    override fun serializeAsValue(value: T): String = Json.encodeToString(value)
}