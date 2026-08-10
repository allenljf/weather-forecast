package com.allenljf.weatherforecast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the temperature unit used for display. Defaults to
 * [TemperatureUnit.DEFAULT] until the user picks one.
 */
class TemperatureUnitDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val unit: Flow<TemperatureUnit> = dataStore.data.map { preferences ->
        TemperatureUnit.fromName(preferences[KEY_TEMPERATURE_UNIT])
    }

    suspend fun setUnit(unit: TemperatureUnit) {
        dataStore.edit { preferences ->
            preferences[KEY_TEMPERATURE_UNIT] = unit.name
        }
    }

    private companion object {
        val KEY_TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }
}
