package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import javax.inject.Inject

/**
 * Saves a city and makes it the selected one, so the user immediately
 * sees the forecast for the city they just added.
 */
class AddCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(city: City) {
        cityRepository.addCity(city)
        cityRepository.selectCity(city.id)
    }
}
