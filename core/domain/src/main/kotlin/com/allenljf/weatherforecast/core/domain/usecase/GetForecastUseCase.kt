package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import javax.inject.Inject

/** Fetches a fresh forecast for [city], caching it on success. */
class GetForecastUseCase @Inject constructor(
    private val forecastRepository: ForecastRepository,
) {
    suspend operator fun invoke(city: City): AppResult<CachedForecast> =
        forecastRepository.refreshForecast(city)
}
