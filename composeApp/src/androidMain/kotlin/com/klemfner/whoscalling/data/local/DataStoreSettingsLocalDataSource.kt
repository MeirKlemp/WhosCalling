package com.klemfner.whoscalling.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsLocalDataSource(
    private val context: Context,
    private val defaultIso: String,
) : SettingsLocalDataSource {

    override val countryIso: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[KEY_COUNTRY_ISO] ?: defaultIso }

    override suspend fun setCountryIso(iso: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_COUNTRY_ISO] = iso
        }
    }

    companion object {
        private val KEY_COUNTRY_ISO = stringPreferencesKey("country_iso")
    }
}
