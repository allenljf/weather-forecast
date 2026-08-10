package com.allenljf.weatherforecast.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Open-Meteo Air Quality API.
 *
 * Note: Open-Meteo omits the `current` block entirely when no measurement is
 * available for the requested location, and individual measurements can be
 * `null`, so everything below the root is nullable with a default.
 */
@Serializable
data class AirQualityResponseDto(
    @SerialName("current") val current: AirQualityCurrentDto? = null,
)

@Serializable
data class AirQualityCurrentDto(
    @SerialName("time") val time: String,
    @SerialName("european_aqi") val europeanAqi: Int? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    @SerialName("pm10") val pm10: Double? = null,
)
