package com.allenljf.weatherforecast.core.data.mapper

import com.allenljf.weatherforecast.core.domain.model.WeatherCondition
import com.allenljf.weatherforecast.core.network.model.CurrentDto
import com.allenljf.weatherforecast.core.network.model.DailyDto
import com.allenljf.weatherforecast.core.network.model.ForecastResponseDto
import com.allenljf.weatherforecast.core.network.model.HourlyDto
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ForecastMappersTest {

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
            time = listOf("2026-08-10T12:00", "2026-08-10T13:00", "2026-08-10T14:00"),
            temperature2m = listOf(31.5, 32.0, 30.8),
            weatherCode = listOf(0, 61, 95),
        ),
        daily = DailyDto(
            time = listOf("2026-08-10", "2026-08-11"),
            weatherCode = listOf(3, 80),
            temperature2mMax = listOf(33.1, 29.9),
            temperature2mMin = listOf(26.4, 25.0),
        ),
    )

    @Test
    fun `maps current weather with all fields`() {
        val forecast = response.toDomain()

        val current = forecast.current
        assertEquals(31.5, current.temperature, 0.0)
        assertEquals(35.2, current.feelsLike, 0.0)
        assertEquals(68, current.humidity)
        assertEquals(12.4, current.windSpeed, 0.0)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, current.condition)
        assertEquals(LocalDateTime.of(2026, 8, 10, 12, 0), current.time)
    }

    @Test
    fun `zips hourly parallel arrays into hourly forecasts`() {
        val hourly = response.toDomain().hourly

        assertEquals(3, hourly.size)

        assertEquals(LocalDateTime.of(2026, 8, 10, 12, 0), hourly[0].time)
        assertEquals(31.5, hourly[0].temperature, 0.0)
        assertEquals(WeatherCondition.CLEAR, hourly[0].condition)

        assertEquals(LocalDateTime.of(2026, 8, 10, 13, 0), hourly[1].time)
        assertEquals(32.0, hourly[1].temperature, 0.0)
        assertEquals(WeatherCondition.RAIN, hourly[1].condition)

        assertEquals(LocalDateTime.of(2026, 8, 10, 14, 0), hourly[2].time)
        assertEquals(30.8, hourly[2].temperature, 0.0)
        assertEquals(WeatherCondition.THUNDERSTORM, hourly[2].condition)
    }

    @Test
    fun `zips daily parallel arrays into daily forecasts`() {
        val daily = response.toDomain().daily

        assertEquals(2, daily.size)

        assertEquals(LocalDate.of(2026, 8, 10), daily[0].date)
        assertEquals(26.4, daily[0].minTemperature, 0.0)
        assertEquals(33.1, daily[0].maxTemperature, 0.0)
        assertEquals(WeatherCondition.OVERCAST, daily[0].condition)

        assertEquals(LocalDate.of(2026, 8, 11), daily[1].date)
        assertEquals(25.0, daily[1].minTemperature, 0.0)
        assertEquals(29.9, daily[1].maxTemperature, 0.0)
        assertEquals(WeatherCondition.RAIN_SHOWERS, daily[1].condition)
    }
}
