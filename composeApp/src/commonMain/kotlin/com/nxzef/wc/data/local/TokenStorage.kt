package com.nxzef.wc.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TokenStorage(
    val dataStore: DataStore<Preferences>
) {
    companion object {
        val TOKEN_KEY         = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY       = stringPreferencesKey("user_id")
        val USER_NAME_KEY     = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY    = stringPreferencesKey("user_email")
        val USER_ROLE_KEY     = stringPreferencesKey("user_role")
        val TEAM_ID_KEY       = stringPreferencesKey("team_id")
        val TEAM_NAME_KEY     = stringPreferencesKey("team_name")
        val TEAM_INVITE_CODE_KEY = stringPreferencesKey("team_invite_code")
        val HAS_LAUNCHED_BEFORE_KEY = booleanPreferencesKey("has_launched_before")
        val PIPELINE_VIEW_LAYOUT_KEY = stringPreferencesKey("pipeline_view_layout")
        val PIPELINE_COLUMN_WIDTHS_KEY = stringPreferencesKey("pipeline_column_widths")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    suspend fun saveSession(
        token: String,
        refreshToken: String,
        id: String,
        name: String,
        email: String,
        role: String,
        teamId: String? = null,
        teamName: String? = null,
        teamInviteCode: String? = null
    ) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY]         = token
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY]       = id
            prefs[USER_NAME_KEY]     = name
            prefs[USER_EMAIL_KEY]    = email
            prefs[USER_ROLE_KEY]     = role
            if (teamId != null) prefs[TEAM_ID_KEY] = teamId else prefs.remove(TEAM_ID_KEY)
            if (teamName != null) prefs[TEAM_NAME_KEY] = teamName else prefs.remove(TEAM_NAME_KEY)
            if (teamInviteCode != null) prefs[TEAM_INVITE_CODE_KEY] = teamInviteCode else prefs.remove(TEAM_INVITE_CODE_KEY)
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_NAME_KEY)
            prefs.remove(USER_EMAIL_KEY)
            prefs.remove(USER_ROLE_KEY)
            prefs.remove(TEAM_ID_KEY)
            prefs.remove(TEAM_NAME_KEY)
            prefs.remove(TEAM_INVITE_CODE_KEY)
        }
    }

    suspend fun hasLaunchedBefore(): Boolean =
        dataStore.data.first()[HAS_LAUNCHED_BEFORE_KEY] == true

    suspend fun markLaunchedBefore() {
        dataStore.edit { it[HAS_LAUNCHED_BEFORE_KEY] = true }
    }

    suspend fun getPipelineViewLayout(): String? =
        dataStore.data.firstOrNull()?.get(PIPELINE_VIEW_LAYOUT_KEY)

    suspend fun savePipelineViewLayout(layout: String) {
        dataStore.edit { it[PIPELINE_VIEW_LAYOUT_KEY] = layout }
    }

    suspend fun getPipelineColumnWidths(): String? =
        dataStore.data.firstOrNull()?.get(PIPELINE_COLUMN_WIDTHS_KEY)

    suspend fun savePipelineColumnWidths(encoded: String) {
        dataStore.edit { it[PIPELINE_COLUMN_WIDTHS_KEY] = encoded }
    }
}
