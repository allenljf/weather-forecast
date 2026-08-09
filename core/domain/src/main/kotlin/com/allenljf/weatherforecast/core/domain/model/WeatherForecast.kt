package com.allenljf.weatherforecast.core.domain.model

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
)

data class DailyForecast(
    val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val condition: WeatherCondition,
)

data class WeatherForecast(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
)
