package com.allenljf.weatherforecast.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.allenljf.weatherforecast.core.domain.model.WeatherCondition

val WeatherCondition.icon: ImageVector
    get() = when (this) {
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> Icons.Filled.WbSunny
        WeatherCondition.PARTLY_CLOUDY -> Icons.Filled.WbCloudy
        WeatherCondition.OVERCAST -> Icons.Filled.Cloud
        WeatherCondition.FOG -> Icons.Filled.Dehaze
        WeatherCondition.DRIZZLE,
        WeatherCondition.FREEZING_DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.FREEZING_RAIN,
        WeatherCondition.RAIN_SHOWERS,
        -> Icons.Filled.Grain

        WeatherCondition.SNOW,
        WeatherCondition.SNOW_GRAINS,
        WeatherCondition.SNOW_SHOWERS,
        -> Icons.Filled.AcUnit

        WeatherCondition.THUNDERSTORM,
        WeatherCondition.THUNDERSTORM_WITH_HAIL,
        -> Icons.Filled.Bolt

        WeatherCondition.UNKNOWN -> Icons.Filled.HelpOutline
    }

val WeatherCondition.label: String
    get() = when (this) {
        WeatherCondition.CLEAR -> "Clear sky"
        WeatherCondition.MAINLY_CLEAR -> "Mainly clear"
        WeatherCondition.PARTLY_CLOUDY -> "Partly cloudy"
        WeatherCondition.OVERCAST -> "Overcast"
        WeatherCondition.FOG -> "Fog"
        WeatherCondition.DRIZZLE -> "Drizzle"
        WeatherCondition.FREEZING_DRIZZLE -> "Freezing drizzle"
        WeatherCondition.RAIN -> "Rain"
        WeatherCondition.FREEZING_RAIN -> "Freezing rain"
        WeatherCondition.SNOW -> "Snow"
        WeatherCondition.SNOW_GRAINS -> "Snow grains"
        WeatherCondition.RAIN_SHOWERS -> "Rain showers"
        WeatherCondition.SNOW_SHOWERS -> "Snow showers"
        WeatherCondition.THUNDERSTORM -> "Thunderstorm"
        WeatherCondition.THUNDERSTORM_WITH_HAIL -> "Thunderstorm with hail"
        WeatherCondition.UNKNOWN -> "Unknown"
    }

@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = condition.icon,
        contentDescription = condition.label,
        modifier = modifier,
        tint = MaterialTheme.colorScheme.primary,
    )
}
