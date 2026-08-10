package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.data.mapper.toDomain
import com.allenljf.weatherforecast.core.data.cache.ForecastCacheSerializer
import com.allenljf.weatherforecast.core.database.dao.ForecastCacheDao
import com.allenljf.weatherforecast.core.database.entity.CachedForecastEntity
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.network.api.ForecastApi
import com.allenljf.weatherforecast.core.network.model.CurrentDto
import com.allenljf.weatherforecast.core.network.model.DailyDto
import com.allenljf.weatherforecast.core.network.model.ForecastResponseDto
import com.allenljf.weatherforecast.core.network.model.HourlyDto
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ForecastRepositoryImplTest {

    private val forecastApi = mockk<ForecastApi>()
    private val forecastCacheDao = mockk<ForecastCacheDao>(relaxUnitFun = true)
    private val serializer = ForecastCacheSerializer()
    private val fetchedAt = Instant.parse("2026-08-10T04:00:00Z")
    private val clock = Clock.fixed(fetchedAt, ZoneOffset.UTC)
    private val repository = ForecastRepositoryImpl(
        forecastApi,
        forecastCacheDao,
        serializer,
        clock,
    )

    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)

    private val response = ForecastResponseDto(
        latitude = 25.03,
        longitude = 121.56,
        timezone = "Asia/Taipei",
        current = CurrentDto(
            time = "2026-08-10T12:00",
            temperature2m = 31.5,
            apparentTemperature = 35.2,
            relativeHumidity2m = 68,
            weatherCode = 2,
            windSpeed10m = 12.4,
        ),
        hourly = HourlyDto(
            time = listOf("2026-08-10T12:00"),
            temperature2m = listOf(31.5),
            weatherCode = listOf(0),
        ),
        daily = DailyDto(
            time = listOf("2026-08-10"),
            weatherCode = listOf(3),
            temperature2mMax = listOf(33.1),
            temperature2mMin = listOf(26.4),
        ),
    )

    @Test
    fun `returns mapped forecast on success`() = runTest {
        coEvery { forecastApi.getForecast(latitude = 25.03, longitude = 121.56) } returns response

        val result = repository.refreshForecast(taipei)

        assertEquals(AppResult.Success(CachedForecast(response.toDomain(), fetchedAt)), result)
    }

    @Test
    fun `maps IOException to network error`() = runTest {
        coEvery { forecastApi.getForecast(latitude = any(), longitude = any()) } throws IOException("no connectivity")

        val result = repository.refreshForecast(taipei)

        assertEquals(AppResult.Error(AppError.Network), result)
    }

    @Test
    fun `maps HttpException to server error with status code`() = runTest {
        val httpException = HttpException(Response.error<Any>(503, "".toResponseBody()))
        coEvery { forecastApi.getForecast(latitude = any(), longitude = any()) } throws httpException

        val result = repository.refreshForecast(taipei)

        assertEquals(AppResult.Error(AppError.Server(503)), result)
    }

    @Test
    fun `maps unexpected exception to unknown error with message`() = runTest {
        coEvery { forecastApi.getForecast(latitude = any(), longitude = any()) } throws RuntimeException("boom")

        val result = repository.refreshForecast(taipei)

        assertEquals(AppResult.Error(AppError.Unknown("boom")), result)
    }

    @Test
    fun `successful refresh writes the forecast to the cache`() = runTest {
        coEvery { forecastApi.getForecast(latitude = 25.03, longitude = 121.56) } returns response
        val stored = mutableListOf<CachedForecastEntity>()
        coEvery { forecastCacheDao.upsert(any()) } answers {
            stored += firstArg<CachedForecastEntity>()
        }

        repository.refreshForecast(taipei)

        val entity = stored.single()
        assertEquals(taipei.id, entity.cityId)
        assertEquals(fetchedAt.toEpochMilli(), entity.fetchedAtMillis)
        assertEquals(response.toDomain(), serializer.decode(entity.payload))
    }

    @Test
    fun `getCachedForecast returns null when nothing is cached`() = runTest {
        coEvery { forecastCacheDao.getByCityId(taipei.id) } returns null

        assertEquals(null, repository.getCachedForecast(taipei))
    }

    @Test
    fun `getCachedForecast decodes a stored payload`() = runTest {
        val forecast = response.toDomain()
        coEvery { forecastCacheDao.getByCityId(taipei.id) } returns CachedForecastEntity(
            cityId = taipei.id,
            payload = serializer.encode(forecast),
            fetchedAtMillis = fetchedAt.toEpochMilli(),
        )

        assertEquals(CachedForecast(forecast, fetchedAt), repository.getCachedForecast(taipei))
    }

    @Test
    fun `corrupt cache payload degrades to no cache instead of crashing`() = runTest {
        coEvery { forecastCacheDao.getByCityId(taipei.id) } returns CachedForecastEntity(
            cityId = taipei.id,
            payload = "not json",
            fetchedAtMillis = fetchedAt.toEpochMilli(),
        )

        assertEquals(null, repository.getCachedForecast(taipei))
    }
}
