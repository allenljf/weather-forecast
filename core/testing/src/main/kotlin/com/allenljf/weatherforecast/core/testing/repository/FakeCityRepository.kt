package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * In-memory [CityRepository] for tests. State can be seeded and inspected directly.
 */
class FakeCityRepository : CityRepository {

    val savedCities = MutableStateFlow<List<City>>(emptyList())
    val selectedCityId = MutableStateFlow<Long?>(null)

    /** Result returned by [searchCities]. */
    var searchResult: AppResult<List<City>> = AppResult.Success(emptyList())

    override fun observeSavedCities(): Flow<List<City>> = savedCities

    override fun observeSelectedCity(): Flow<City?> =
        combine(savedCities, selectedCityId) { cities, id ->
            cities.firstOrNull { it.id == id }
        }

    override suspend fun searchCities(query: String): AppResult<List<City>> = searchResult

    override suspend fun addCity(city: City) {
        savedCities.value = savedCities.value.filter { it.id != city.id } + city
    }

    override suspend fun removeCity(cityId: Long) {
        savedCities.value = savedCities.value.filter { it.id != cityId }
    }

    override suspend fun selectCity(cityId: Long) {
        selectedCityId.value = cityId
    }
}
