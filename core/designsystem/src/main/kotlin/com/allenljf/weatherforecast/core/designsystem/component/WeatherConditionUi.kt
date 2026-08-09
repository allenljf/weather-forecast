package com.allenljf.weatherforecast.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.allenljf.weatherforecast.core.designsystem.R
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

        WeatherCondition.UNKNOWN -> Icons.AutoMirrored.Filled.HelpOutline
    }

@get:StringRes
val WeatherCondition.labelRes: Int
    get() = when (this) {
        WeatherCondition.CLEAR -> R.string.condition_clear
        WeatherCondition.MAINLY_CLEAR -> R.string.condition_mainly_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.condition_partly_cloudy
        WeatherCondition.OVERCAST -> R.string.condition_overcast
        WeatherCondition.FOG -> R.string.condition_fog
        WeatherCondition.DRIZZLE -> R.string.condition_drizzle
        WeatherCondition.FREEZING_DRIZZLE -> R.string.condition_freezing_drizzle
        WeatherCondition.RAIN -> R.string.condition_rain
        WeatherCondition.FREEZING_RAIN -> R.string.condition_freezing_rain
        WeatherCondition.SNOW -> R.string.condition_snow
        WeatherCondition.SNOW_GRAINS -> R.string.condition_snow_grains
        WeatherCondition.RAIN_SHOWERS -> R.string.condition_rain_showers
        WeatherCondition.SNOW_SHOWERS -> R.string.condition_snow_showers
        WeatherCondition.THUNDERSTORM -> R.string.condition_thunderstorm
        WeatherCondition.THUNDERSTORM_WITH_HAIL -> R.string.condition_thunderstorm_with_hail
        WeatherCondition.UNKNOWN -> R.string.condition_unknown
    }

@Composable
fun WeatherCondition.localizedLabel(): String = stringResource(labelRes)

@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = condition.icon,
        contentDescription = condition.localizedLabel(),
        modifier = modifier,
        tint = MaterialTheme.colorScheme.primary,
    )
}
