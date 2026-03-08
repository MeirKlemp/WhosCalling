package com.klemfner.whoscalling.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.util.defaultCountryIso
import com.klemfner.whoscalling.util.defaultRouterIp
import com.klemfner.whoscalling.util.defaultTouchMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsLocalDataSource(
    private val context: Context,
) : SettingsLocalDataSource {

    override val preferences: Flow<UserPreferences> = context.settingsDataStore.data
        .map { prefs -> prefs.toUserPreferences(context) }

    override suspend fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        context.settingsDataStore.edit { prefs ->
            val updated = update(prefs.toUserPreferences(context))
            prefs[KEY_COUNTRY_ISO] = updated.countryIso
            prefs[KEY_TOUCH_MODE] = updated.touchMode
            prefs[KEY_ROUTER_IP] = updated.routerIp
            prefs[KEY_REFRESH_RATE_SECONDS] = updated.refreshRateSeconds
        }
    }

    companion object {
        private val KEY_COUNTRY_ISO = stringPreferencesKey("country_iso")
        private val KEY_TOUCH_MODE = booleanPreferencesKey("touch_mode")
        private val KEY_ROUTER_IP = stringPreferencesKey("router_ip")
        private val KEY_REFRESH_RATE_SECONDS = longPreferencesKey("refresh_rate_seconds")

        private fun Preferences.toUserPreferences(context: Context) = UserPreferences(
            countryIso = this[KEY_COUNTRY_ISO] ?: defaultCountryIso(),
            touchMode = this[KEY_TOUCH_MODE] ?: defaultTouchMode(),
            routerIp = this[KEY_ROUTER_IP] ?: defaultRouterIp(context),
            refreshRateSeconds = this[KEY_REFRESH_RATE_SECONDS] ?: UserPreferences.DEFAULT_REFRESH_RATE_SECONDS,
        )
    }
}
