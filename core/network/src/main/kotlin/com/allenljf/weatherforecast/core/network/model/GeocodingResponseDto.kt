package com.allenljf.weatherforecast.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Open-Meteo Geocoding API.
 *
 * Note: when there is no match, Open-Meteo omits the `results` field entirely,
 * so it must be nullable with a default.
 */
@Serializable
data class GeocodingResponseDto(
    @SerialName("results") val results: List<GeocodingResultDto>? = null,
)

@Serializable
data class GeocodingResultDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("country") val country: String? = null,
    @SerialName("admin1") val admin1: String? = null,
)
