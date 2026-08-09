package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.WeatherCondition
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GetForecastUseCaseTest {

    private val forecastRepository = mockk<ForecastRepository>()
    private val useCase = GetForecastUseCase(forecastRepository)

    @Test
    fun `returns forecast from repository`() = runTest {
        val forecast = WeatherForecast(
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
        )
        coEvery { forecastRepository.getForecast(taipei) } returns AppResult.Success(forecast)

        assertEquals(AppResult.Success(forecast), useCase(taipei))
    }

    @Test
    fun `propagates repository error`() = runTest {
        coEvery { forecastRepository.getForecast(taipei) } returns AppResult.Error(AppError.Server(500))

        assertEquals(AppResult.Error(AppError.Server(500)), useCase(taipei))
    }

    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)
}
