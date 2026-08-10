package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Emits the city whose forecast should be shown: the explicitly selected city,
 * falling back to the first saved city when nothing was selected yet.
 */
class ObserveSelectedCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    operator fun invoke(): Flow<City?> = combine(
        cityRepository.observeSelectedCity(),
        cityRepository.observeSavedCities(),
    ) { selected, saved ->
        selected ?: saved.firstOrNull()
    }
}
