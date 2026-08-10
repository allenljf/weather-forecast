package com.allenljf.weatherforecast.core.domain.repository

import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeTemperatureUnit(): Flow<TemperatureUnit>

    suspend fun setTemperatureUnit(unit: TemperatureUnit)
}
