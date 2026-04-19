package com.nxzef.wc.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            System.getProperty("user.home")
                .plus("/.wc/$DATA_STORE_FILE_NAME")
                .toPath()
        }
    )
}