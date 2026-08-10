package com.allenljf.weatherforecast.core.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val time: LocalDateTime,
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val condition: WeatherCondition,
    /** Chance of precipitation, 0-100. Null when the API omits it. */
    val precipitationProbability: Int? = null,
)

data class DailyForecast(
    val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val condition: WeatherCondition,
    /** Highest chance of precipitation during the day, 0-100. */
    val precipitationProbability: Int? = null,
    /** Local sunrise/sunset for the city; null if the API omits them. */
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null,
)

data class WeatherForecast(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
) {
    /** Today's entry, which carries the sunrise/sunset shown on the current card. */
    val today: DailyForecast? get() = daily.firstOrNull()
}

/**
 * A forecast plus when it was retrieved, so the UI can show how fresh it is and
 * mark cached data as stale.
 */
data class CachedForecast(
    val forecast: WeatherForecast,
    val fetchedAt: Instant,
)
