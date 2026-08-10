package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.WeatherCondition
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetForecastUseCaseTest {

    private val forecastRepository = mockk<ForecastRepository>()
    private val useCase = GetForecastUseCase(forecastRepository)
    private val cachedUseCase = GetCachedForecastUseCase(forecastRepository)

    @Test
    fun `returns refreshed forecast from repository`() = runTest {
        coEvery { forecastRepository.refreshForecast(taipei) } returns AppResult.Success(cached)

        assertEquals(AppResult.Success(cached), useCase(taipei))
    }

    @Test
    fun `propagates repository error`() = runTest {
        coEvery { forecastRepository.refreshForecast(taipei) } returns
            AppResult.Error(AppError.Server(500))

        assertEquals(AppResult.Error(AppError.Server(500)), useCase(taipei))
    }

    @Test
    fun `cached use case returns whatever the repository has`() = runTest {
        coEvery { forecastRepository.getCachedForecast(taipei) } returns cached

        assertEquals(cached, cachedUseCase(taipei))
    }

    @Test
    fun `cached use case returns null when nothing is cached`() = runTest {
        coEvery { forecastRepository.getCachedForecast(taipei) } returns null

        assertNull(cachedUseCase(taipei))
    }

    private val taipei =
        City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)

    private val cached = CachedForecast(
        forecast = WeatherForecast(
            current = CurrentWeather(
                temperature = 28.5,
                feelsLike = 31.0,
                humidity = 70,
                windSpeed = 12.0,
                condition = WeatherCondition.PARTLY_CLOUDY,
                time = LocalDateTime.of(2026, 8, 10, 12, 0),
            ),
            hourly = emptyList(),
            daily = emptyList(),
        ),
        fetchedAt = Instant.parse("2026-08-10T12:00:00Z"),
    )
}
