package com.allenljf.weatherforecast.core.domain.model

/**
 * Air quality for a city, using the European AQI scale.
 */
data class AirQuality(
    val europeanAqi: Int,
    val pm2_5: Double?,
    val pm10: Double?,
) {
    val level: AirQualityLevel get() = AirQualityLevel.fromEuropeanAqi(europeanAqi)
}

/**
 * European AQI bands (https://www.eea.europa.eu/themes/air/air-quality-index).
 */
enum class AirQualityLevel {
    GOOD,
    FAIR,
    MODERATE,
    POOR,
    VERY_POOR,
    EXTREMELY_POOR,
    ;

    companion object {
        fun fromEuropeanAqi(aqi: Int): AirQualityLevel = when {
            aqi <= 20 -> GOOD
            aqi <= 40 -> FAIR
            aqi <= 60 -> MODERATE
            aqi <= 80 -> POOR
            aqi <= 100 -> VERY_POOR
            else -> EXTREMELY_POOR
        }
    }
}
