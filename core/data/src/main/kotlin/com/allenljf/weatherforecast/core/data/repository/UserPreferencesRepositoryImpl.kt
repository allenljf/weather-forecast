package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.datastore.TemperatureUnitDataSource
import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import com.allenljf.weatherforecast.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: TemperatureUnitDataSource,
) : UserPreferencesRepository {

    override fun observeTemperatureUnit(): Flow<TemperatureUnit> = dataSource.unit

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = dataSource.setUnit(unit)
}
