package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.data.cache.ForecastCacheSerializer
import com.allenljf.weatherforecast.core.data.mapper.toDomain
import com.allenljf.weatherforecast.core.database.dao.ForecastCacheDao
import com.allenljf.weatherforecast.core.database.entity.CachedForecastEntity
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import com.allenljf.weatherforecast.core.network.api.ForecastApi
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class ForecastRepositoryImpl @Inject constructor(
    private val forecastApi: ForecastApi,
    private val forecastCacheDao: ForecastCacheDao,
    private val serializer: ForecastCacheSerializer,
    private val clock: Clock,
) : ForecastRepository {

    override suspend fun getCachedForecast(city: City): CachedForecast? {
        val entity = forecastCacheDao.getByCityId(city.id) ?: return null
        val forecast = serializer.decode(entity.payload) ?: return null
        return CachedForecast(forecast, Instant.ofEpochMilli(entity.fetchedAtMillis))
    }

    override suspend fun refreshForecast(city: City): AppResult<CachedForecast> = safeApiCall {
        val forecast = forecastApi
            .getForecast(latitude = city.latitude, longitude = city.longitude)
            .toDomain()
        val fetchedAt = clock.instant()

        forecastCacheDao.upsert(
            CachedForecastEntity(
                cityId = city.id,
                payload = serializer.encode(forecast),
                fetchedAtMillis = fetchedAt.toEpochMilli(),
            ),
        )

        CachedForecast(forecast, fetchedAt)
    }
}
