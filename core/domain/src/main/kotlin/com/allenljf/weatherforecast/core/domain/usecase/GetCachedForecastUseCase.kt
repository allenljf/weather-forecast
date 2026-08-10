package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import javax.inject.Inject

/** Last cached forecast for [city], so the UI has something to show offline. */
class GetCachedForecastUseCase @Inject constructor(
    private val forecastRepository: ForecastRepository,
) {
    suspend operator fun invoke(city: City): CachedForecast? =
        forecastRepository.getCachedForecast(city)
}
