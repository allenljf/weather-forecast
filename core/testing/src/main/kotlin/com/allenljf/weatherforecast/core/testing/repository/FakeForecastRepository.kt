package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository

/**
 * [ForecastRepository] for tests: returns a configurable result per city id,
 * falling back to [defaultResult].
 */
class FakeForecastRepository : ForecastRepository {

    var defaultResult: AppResult<WeatherForecast> = AppResult.Error(AppError.Unknown("not stubbed"))
    private val resultsByCityId = mutableMapOf<Long, AppResult<WeatherForecast>>()

    var lastRequestedCity: City? = null
        private set

    fun setForecast(cityId: Long, result: AppResult<WeatherForecast>) {
        resultsByCityId[cityId] = result
    }

    override suspend fun getForecast(city: City): AppResult<WeatherForecast> {
        lastRequestedCity = city
        return resultsByCityId[city.id] ?: defaultResult
    }
}
