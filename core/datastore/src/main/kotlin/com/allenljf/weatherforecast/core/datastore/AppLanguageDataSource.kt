package com.allenljf.weatherforecast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the user's UI language choice. Defaults to [AppLanguage.DEFAULT]
 * when the user has never picked one.
 */
class AppLanguageDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val language: Flow<AppLanguage> = dataStore.data.map { preferences ->
        AppLanguage.fromTag(preferences[KEY_LANGUAGE])
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language.tag
        }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }
}
