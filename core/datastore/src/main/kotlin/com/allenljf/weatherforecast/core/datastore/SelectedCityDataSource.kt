package com.allenljf.weatherforecast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the id of the city currently selected for the forecast screen.
 */
class SelectedCityDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** Emits the selected city id, or `null` when none has been selected. */
    val selectedCityId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CITY_ID]
    }

    suspend fun setSelectedCityId(id: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECTED_CITY_ID] = id
        }
    }

    suspend fun clearSelectedCityId() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_SELECTED_CITY_ID)
        }
    }

    private companion object {
        val KEY_SELECTED_CITY_ID = longPreferencesKey("selected_city_id")
    }
}
