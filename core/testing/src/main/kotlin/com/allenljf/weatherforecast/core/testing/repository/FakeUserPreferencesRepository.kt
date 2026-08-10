package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import com.allenljf.weatherforecast.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository : UserPreferencesRepository {
    val temperatureUnit = MutableStateFlow(TemperatureUnit.DEFAULT)

    override fun observeTemperatureUnit(): Flow<TemperatureUnit> = temperatureUnit

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        temperatureUnit.value = unit
    }
}
