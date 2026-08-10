package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Removes a saved city. If the removed city was the selected one, selection
 * moves to the first remaining saved city (if any).
 */
class RemoveCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(cityId: Long) {
        val wasSelected = cityRepository.observeSelectedCity().first()?.id == cityId

        cityRepository.removeCity(cityId)

        if (wasSelected) {
            cityRepository.observeSavedCities().first()
                .firstOrNull { it.id != cityId }
                ?.let { cityRepository.selectCity(it.id) }
        }
    }
}
