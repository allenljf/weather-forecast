package com.allenljf.weatherforecast.feature.forecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.weatherforecast.core.designsystem.component.EmptyState
import com.allenljf.weatherforecast.core.designsystem.component.ErrorState
import com.allenljf.weatherforecast.core.designsystem.component.LoadingState
import com.allenljf.weatherforecast.core.designsystem.component.WeatherConditionIcon
import com.allenljf.weatherforecast.core.designsystem.component.localizedLabel
import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.designsystem.component.color
import com.allenljf.weatherforecast.core.designsystem.component.labelRes
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import androidx.compose.ui.text.intl.Locale as ComposeLocale
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastRoute(
    onCitiesClick: () -> Unit,
    viewModel: ForecastViewModel = hiltViewModel(),
) {
    val screenState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        screenState = screenState,
        onCitiesClick = onCitiesClick,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        onLanguageSelected = viewModel::onLanguageSelected,
        onToggleTemperatureUnit = viewModel::onToggleTemperatureUnit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    screenState: ForecastScreenState,
    onCitiesClick: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onToggleTemperatureUnit: () -> Unit = {},
) {
    val uiState = screenState.forecast
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is ForecastUiState.Success -> uiState.city.name
                        is ForecastUiState.Error -> uiState.city.name
                        else -> stringResource(R.string.forecast_default_title)
                    }
                    Text(text = title, modifier = Modifier.testTag("forecast_title"))
                },
                actions = {
                    TemperatureUnitButton(
                        unit = screenState.temperatureUnit,
                        onClick = onToggleTemperatureUnit,
                    )
                    LanguageMenu(
                        current = screenState.language,
                        onLanguageSelected = onLanguageSelected,
                    )
                    SearchCitiesButton(onClick = onCitiesClick)
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            screenState.banner?.let { banner ->
                ForecastBannerBar(banner = banner, onRetry = onRetry)
            }

            PullToRefreshBox(
                isRefreshing = screenState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                ForecastBody(
                    uiState = uiState,
                    unit = screenState.temperatureUnit,
                    airQuality = screenState.airQuality,
                    onRetry = onRetry,
                )
            }
        }
    }
}

/** Toggles between °C and °F; the label always shows the unit in use. */
@Composable
private fun TemperatureUnitButton(
    unit: TemperatureUnit,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.testTag("temperature_unit_button"),
    ) {
        Text(text = unit.symbol, style = MaterialTheme.typography.titleMedium)
    }
}

/** Language picker; only the two shipped locales are offered. */
@Composable
private fun LanguageMenu(
    current: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("language_button"),
        ) {
            Icon(
                imageVector = Icons.Filled.Translate,
                contentDescription = stringResource(R.string.change_language),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("language_option_${language.tag}"),
                    text = { Text(text = stringResource(language.labelRes)) },
                    trailingIcon = {
                        if (language == current) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onLanguageSelected(language)
                    },
                )
            }
        }
    }
}

/**
 * Primary way into city search. Uses a filled, rounded container so it stands
 * out against the app bar instead of reading as a plain icon.
 */
@Composable
private fun SearchCitiesButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 12.dp)
            .size(44.dp)
            .testTag("cities_button"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.search_cities),
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/**
 * Persistent top banner. Deliberately has no dismiss action: dismissing would
 * remove the only way to trigger a manual retry.
 */
@Composable
private fun ForecastBannerBar(
    banner: ForecastBanner,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("error_banner"),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (banner) {
                    ForecastBanner.Offline -> stringResource(R.string.banner_offline)
                    is ForecastBanner.LoadFailed -> when (banner.error) {
                        is AppError.Server -> stringResource(R.string.banner_server_error)
                        else -> stringResource(R.string.banner_unknown_error)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .testTag("error_banner_message"),
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.testTag("banner_retry_button"),
            ) {
                Text(text = stringResource(R.string.retry_action))
            }
        }
    }
}

@Composable
private fun ForecastBody(
    uiState: ForecastUiState,
    unit: TemperatureUnit,
    airQuality: AirQuality?,
    onRetry: () -> Unit,
) {
    val contentModifier = Modifier.fillMaxSize()

    when (uiState) {
        ForecastUiState.Loading -> LoadingState(modifier = contentModifier)

        ForecastUiState.NoCitySelected -> EmptyState(
            message = stringResource(R.string.no_city_selected),
            modifier = contentModifier,
        )

        is ForecastUiState.Error -> ErrorState(
            message = when (uiState.error) {
                AppError.Network -> stringResource(R.string.forecast_error_network, uiState.city.name)
                is AppError.Server -> stringResource(R.string.forecast_error_server)
                is AppError.Unknown -> stringResource(R.string.forecast_error_unknown, uiState.city.name)
            },
            onRetry = onRetry,
            modifier = contentModifier,
        )

        is ForecastUiState.Success -> ForecastContent(
            uiState = uiState,
            unit = unit,
            airQuality = airQuality,
            modifier = contentModifier,
        )
    }
}

@Composable
private fun ForecastContent(
    uiState: ForecastUiState.Success,
    unit: TemperatureUnit,
    airQuality: AirQuality?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("forecast_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CurrentWeatherCard(uiState = uiState, unit = unit) }

        if (airQuality != null) {
            item { AirQualityCard(airQuality = airQuality) }
        }

        if (uiState.sunrise != null || uiState.sunset != null) {
            item { SunTimesRow(sunrise = uiState.sunrise, sunset = uiState.sunset) }
        }

        item {
            Text(
                text = stringResource(R.string.next_hours),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item { HourlyRow(hourly = uiState.hourly, unit = unit) }

        item {
            Text(
                text = stringResource(R.string.seven_day_forecast),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(uiState.daily, key = { it.date.toEpochDay() }) { day ->
            DailyRow(day = day, unit = unit)
        }

        item { LastUpdatedLabel(fetchedAt = uiState.fetchedAt, isStale = uiState.isStale) }
    }
}

@Composable
private fun CurrentWeatherCard(
    uiState: ForecastUiState.Success,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    val current = uiState.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_weather_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WeatherConditionIcon(
                condition = current.condition,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "${unit.format(current.temperature)}°",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.testTag("current_temperature"),
            )
            Text(
                text = current.condition.localizedLabel(),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.feels_like, unit.format(current.feelsLike)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.humidity, current.humidity),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.wind, current.windSpeed.roundToInt()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun AirQualityCard(airQuality: AirQuality, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("air_quality_card"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(airQuality.level.color, CircleShape),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.air_quality),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = stringResource(airQuality.level.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.aqi_value, airQuality.europeanAqi),
                    style = MaterialTheme.typography.titleMedium,
                )
                airQuality.pm2_5?.let {
                    Text(
                        text = stringResource(R.string.pm25_value, it.roundToInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SunTimesRow(
    sunrise: java.time.LocalDateTime?,
    sunset: java.time.LocalDateTime?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sun_times_row"),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        sunrise?.let {
            SunTime(
                icon = Icons.Filled.WbTwilight,
                label = stringResource(R.string.sunrise),
                time = it.format(HOUR_FORMATTER),
            )
        }
        sunset?.let {
            SunTime(
                icon = Icons.Filled.NightsStay,
                label = stringResource(R.string.sunset),
                time = it.format(HOUR_FORMATTER),
            )
        }
    }
}

@Composable
private fun SunTime(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    time: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = time, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun HourlyRow(
    hourly: List<HourlyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.testTag("hourly_forecast_row"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(hourly, key = { it.time.toString() }) { hour ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = hour.time.format(HOUR_FORMATTER),
                    style = MaterialTheme.typography.labelMedium,
                )
                WeatherConditionIcon(
                    condition = hour.condition,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "${unit.format(hour.temperature)}°",
                    style = MaterialTheme.typography.bodyMedium,
                )
                // Only worth showing when rain is actually plausible.
                hour.precipitationProbability?.takeIf { it > 0 }?.let { probability ->
                    Text(
                        text = stringResource(R.string.precipitation_probability, probability),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRow(
    day: DailyForecast,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_row_${day.date}"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = day.date.dayOfWeek.getDisplayName(
                    TextStyle.SHORT,
                    Locale.forLanguageTag(ComposeLocale.current.toLanguageTag()),
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(56.dp),
            )
            WeatherConditionIcon(
                condition = day.condition,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.condition.localizedLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                day.precipitationProbability?.takeIf { it > 0 }?.let { probability ->
                    Text(
                        text = stringResource(R.string.precipitation_probability, probability),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.min_max_temperature,
                    unit.format(day.minTemperature),
                    unit.format(day.maxTemperature),
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

/** Shows when the data was fetched, and flags it when it is cached/stale. */
@Composable
private fun LastUpdatedLabel(
    fetchedAt: java.time.Instant,
    isStale: Boolean,
    modifier: Modifier = Modifier,
) {
    val time = remember(fetchedAt) {
        java.time.LocalDateTime.ofInstant(fetchedAt, java.time.ZoneId.systemDefault())
            .format(HOUR_FORMATTER)
    }
    Text(
        text = if (isStale) {
            stringResource(R.string.last_updated_stale, time)
        } else {
            stringResource(R.string.last_updated, time)
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .testTag("last_updated_label"),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

private val HOUR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
