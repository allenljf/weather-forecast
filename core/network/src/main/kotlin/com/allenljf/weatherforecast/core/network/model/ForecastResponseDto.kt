package com.allenljf.weatherforecast.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("timezone") val timezone: String,
    @SerialName("current") val current: CurrentDto,
    @SerialName("hourly") val hourly: HourlyDto,
    @SerialName("daily") val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    @SerialName("time") val time: String,
    @SerialName("temperature_2m") val temperature2m: Double,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed10m: Double,
)

/**
 * Note: Open-Meteo omits optional blocks (and may emit `null` entries inside an
 * array) when a field is not requested or not available, so the extra fields are
 * nullable with a default.
 */
@Serializable
data class HourlyDto(
    @SerialName("time") val time: List<String>,
    @SerialName("temperature_2m") val temperature2m: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?>? = null,
)

@Serializable
data class DailyDto(
    @SerialName("time") val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val temperature2mMax: List<Double>,
    @SerialName("temperature_2m_min") val temperature2mMin: List<Double>,
    @SerialName("sunrise") val sunrise: List<String>? = null,
    @SerialName("sunset") val sunset: List<String>? = null,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int?>? = null,
)
