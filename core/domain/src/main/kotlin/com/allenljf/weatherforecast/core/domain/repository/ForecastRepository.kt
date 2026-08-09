package com.allenljf.weatherforecast.core.domain.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast

interface ForecastRepository {
    suspend fun getForecast(city: City): AppResult<WeatherForecast>
}
