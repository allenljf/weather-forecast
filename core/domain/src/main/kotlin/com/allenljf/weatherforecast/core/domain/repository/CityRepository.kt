package com.allenljf.weatherforecast.core.domain.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    /** All cities the user has saved, in the order they were added. */
    fun observeSavedCities(): Flow<List<City>>

    /** The city currently selected by the user, or null if none was ever selected. */
    fun observeSelectedCity(): Flow<City?>

    suspend fun searchCities(query: String): AppResult<List<City>>

    suspend fun addCity(city: City)

    suspend fun removeCity(cityId: Long)

    suspend fun selectCity(cityId: Long)
}
