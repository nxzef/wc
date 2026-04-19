package com.nxzef.wc.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(): DataStore<Preferences>

const val DATA_STORE_FILE_NAME = "wc_prefs.preferences_pb"