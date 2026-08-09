package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val forecastRepository: ForecastRepository,
) {
    suspend operator fun invoke(city: City): AppResult<WeatherForecast> =
        forecastRepository.getForecast(city)
}
