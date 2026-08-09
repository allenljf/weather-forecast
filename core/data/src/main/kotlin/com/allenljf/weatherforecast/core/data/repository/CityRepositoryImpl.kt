package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.data.mapper.toDomain
import com.allenljf.weatherforecast.core.data.mapper.toEntity
import com.allenljf.weatherforecast.core.database.dao.CityDao
import com.allenljf.weatherforecast.core.datastore.SelectedCityDataSource
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import com.allenljf.weatherforecast.core.network.api.GeocodingApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CityRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val cityDao: CityDao,
    private val selectedCityDataSource: SelectedCityDataSource,
) : CityRepository {

    override fun observeSavedCities(): Flow<List<City>> =
        cityDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeSelectedCity(): Flow<City?> =
        combine(cityDao.observeAll(), selectedCityDataSource.selectedCityId) { cities, selectedId ->
            cities.firstOrNull { it.id == selectedId }?.toDomain()
        }

    override suspend fun searchCities(query: String): AppResult<List<City>> = safeApiCall {
        geocodingApi.searchCities(query).results.orEmpty().map { it.toDomain() }
    }

    override suspend fun addCity(city: City) {
        val position = (cityDao.maxPosition() ?: -1) + 1
        cityDao.insert(city.toEntity(position))
    }

    override suspend fun removeCity(cityId: Long) {
        cityDao.deleteById(cityId)
    }

    override suspend fun selectCity(cityId: Long) {
        selectedCityDataSource.setSelectedCityId(cityId)
    }
}
