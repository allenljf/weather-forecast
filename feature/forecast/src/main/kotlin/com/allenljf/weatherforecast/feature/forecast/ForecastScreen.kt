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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    screenState: ForecastScreenState,
    onCitiesClick: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
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
                    IconButton(onClick = onCitiesClick, modifier = Modifier.testTag("cities_button")) {
                        Icon(
                            imageVector = Icons.Filled.LocationCity,
                            contentDescription = stringResource(R.string.manage_cities),
                        )
                    }
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
                ForecastBody(uiState = uiState, onRetry = onRetry)
            }
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
                modifier = contentModifier,
            )
        }
}

@Composable
private fun ForecastContent(
    uiState: ForecastUiState.Success,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("forecast_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CurrentWeatherCard(current = uiState.current) }

        item {
            Text(
                text = stringResource(R.string.next_hours),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item { HourlyRow(hourly = uiState.hourly) }

        item {
            Text(
                text = stringResource(R.string.seven_day_forecast),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(uiState.daily, key = { it.date.toEpochDay() }) { day ->
            DailyRow(day = day)
        }
    }
}

@Composable
private fun CurrentWeatherCard(current: CurrentWeather, modifier: Modifier = Modifier) {
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
                text = "${current.temperature.roundToInt()}°",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.testTag("current_temperature"),
            )
            Text(
                text = current.condition.localizedLabel(),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.feels_like, current.feelsLike.roundToInt()),
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
private fun HourlyRow(hourly: List<HourlyForecast>, modifier: Modifier = Modifier) {
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
                    text = "${hour.temperature.roundToInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyForecast, modifier: Modifier = Modifier) {
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
            Text(
                text = day.condition.localizedLabel(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.min_max_temperature, day.minTemperature.roundToInt(), day.maxTemperature.roundToInt()),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

private val HOUR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
