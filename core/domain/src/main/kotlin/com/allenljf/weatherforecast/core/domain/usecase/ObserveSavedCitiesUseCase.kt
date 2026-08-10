package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSavedCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    operator fun invoke(): Flow<List<City>> = cityRepository.observeSavedCities()
}
