package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import java.time.Instant

/**
 * [ForecastRepository] for tests: refresh results are configurable per city id,
 * and successful refreshes populate the cache the way the real one does.
 */
class FakeForecastRepository : ForecastRepository {

    var defaultResult: AppResult<WeatherForecast> = AppResult.Error(AppError.Unknown("not stubbed"))
    private val resultsByCityId = mutableMapOf<Long, AppResult<WeatherForecast>>()
    private val cache = mutableMapOf<Long, CachedForecast>()

    /** Timestamp used for refreshes; tests can advance it to assert freshness. */
    var now: Instant = Instant.parse("2026-08-10T12:00:00Z")

    var lastRequestedCity: City? = null
        private set

    fun setForecast(cityId: Long, result: AppResult<WeatherForecast>) {
        resultsByCityId[cityId] = result
    }

    /** Seeds the cache without going through a refresh, to simulate a cold start. */
    fun seedCache(cityId: Long, forecast: WeatherForecast, fetchedAt: Instant = now) {
        cache[cityId] = CachedForecast(forecast, fetchedAt)
    }

    override suspend fun getCachedForecast(city: City): CachedForecast? = cache[city.id]

    override suspend fun refreshForecast(city: City): AppResult<CachedForecast> {
        lastRequestedCity = city
        return when (val result = resultsByCityId[city.id] ?: defaultResult) {
            is AppResult.Success -> {
                val cached = CachedForecast(result.data, now)
                cache[city.id] = cached
                AppResult.Success(cached)
            }

            is AppResult.Error -> result
        }
    }
}
