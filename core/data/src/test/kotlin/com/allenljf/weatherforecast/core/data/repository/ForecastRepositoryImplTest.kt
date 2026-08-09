package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.data.mapper.toDomain
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.network.api.ForecastApi
import com.allenljf.weatherforecast.core.network.model.CurrentDto
import com.allenljf.weatherforecast.core.network.model.DailyDto
import com.allenljf.weatherforecast.core.network.model.ForecastResponseDto
import com.allenljf.weatherforecast.core.network.model.HourlyDto
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ForecastRepositoryImplTest {

    private val forecastApi = mockk<ForecastApi>()
    private val repository = ForecastRepositoryImpl(forecastApi)

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

        val result = repository.getForecast(taipei)

        assertEquals(AppResult.Success(response.toDomain()), result)
    }

    @Test
    fun `maps IOException to network error`() = runTest {
        coEvery { forecastApi.getForecast(latitude = any(), longitude = any()) } throws IOException("no connectivity")

        val result = repository.getForecast(taipei)

        assertEquals(AppResult.Error(AppError.Network), result)
    }

    @Test
    fun `maps HttpException to server error with status code`() = runTest {
        val httpException = HttpException(Response.error<Any>(503, "".toResponseBody()))
        coEvery { forecastApi.getForecast(latitude = any(), longitude = any()) } throws httpException

        val result = repository.getForecast(taipei)

        assertEquals(AppResult.Error(AppError.Server(503)), result)
    }

    @Test
    fun `maps unexpected exception to unknown error with message`() = runTest {
        coEvery { forecastApi.getForecast(latitude = any(), longitude = any()) } throws RuntimeException("boom")

        val result = repository.getForecast(taipei)

        assertEquals(AppResult.Error(AppError.Unknown("boom")), result)
    }
}
