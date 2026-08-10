package com.allenljf.weatherforecast.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.allenljf.weatherforecast.core.designsystem.R
import com.allenljf.weatherforecast.core.domain.model.AirQualityLevel

@get:StringRes
val AirQualityLevel.labelRes: Int
    get() = when (this) {
        AirQualityLevel.GOOD -> R.string.aqi_good
        AirQualityLevel.FAIR -> R.string.aqi_fair
        AirQualityLevel.MODERATE -> R.string.aqi_moderate
        AirQualityLevel.POOR -> R.string.aqi_poor
        AirQualityLevel.VERY_POOR -> R.string.aqi_very_poor
        AirQualityLevel.EXTREMELY_POOR -> R.string.aqi_extremely_poor
    }

/**
 * Band colour for the European AQI scale. Kept close to the official palette so
 * the colour carries the same meaning users see elsewhere.
 */
val AirQualityLevel.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        AirQualityLevel.GOOD -> Color(0xFF50CCAA)
        AirQualityLevel.FAIR -> Color(0xFF50CCAA).copy(alpha = 0.85f)
        AirQualityLevel.MODERATE -> Color(0xFFF0E641)
        AirQualityLevel.POOR -> Color(0xFFFF5050)
        AirQualityLevel.VERY_POOR -> Color(0xFF960032)
        AirQualityLevel.EXTREMELY_POOR -> Color(0xFF7D2181)
    }
