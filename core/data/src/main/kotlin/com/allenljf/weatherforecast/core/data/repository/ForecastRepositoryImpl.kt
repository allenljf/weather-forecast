package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.data.mapper.toDomain
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import com.allenljf.weatherforecast.core.network.api.ForecastApi
import javax.inject.Inject

class ForecastRepositoryImpl @Inject constructor(
    private val forecastApi: ForecastApi,
) : ForecastRepository {

    override suspend fun getForecast(city: City): AppResult<WeatherForecast> = safeApiCall {
        forecastApi.getForecast(latitude = city.latitude, longitude = city.longitude).toDomain()
    }
}
