package com.allenljf.weatherforecast.feature.forecast

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.weatherforecast.core.designsystem.component.EmptyState
import com.allenljf.weatherforecast.core.designsystem.component.ErrorState
import com.allenljf.weatherforecast.core.designsystem.component.LoadingState
import com.allenljf.weatherforecast.core.designsystem.component.WeatherConditionIcon
import com.allenljf.weatherforecast.core.designsystem.component.label
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastRoute(
    onCitiesClick: () -> Unit,
    viewModel: ForecastViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        uiState = uiState,
        onCitiesClick = onCitiesClick,
        onRetry = viewModel::onRetry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    uiState: ForecastUiState,
    onCitiesClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is ForecastUiState.Success -> uiState.city.name
                        is ForecastUiState.Error -> uiState.city.name
                        else -> "Weather Forecast"
                    }
                    Text(text = title, modifier = Modifier.testTag("forecast_title"))
                },
                actions = {
                    IconButton(onClick = onCitiesClick, modifier = Modifier.testTag("cities_button")) {
                        Icon(
                            imageVector = Icons.Filled.LocationCity,
                            contentDescription = "Manage cities",
                        )
                    }
                },
            )
        },
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)

        when (uiState) {
            ForecastUiState.Loading -> LoadingState(modifier = contentModifier)

            ForecastUiState.NoCitySelected -> EmptyState(
                message = "No city selected. Add a city to see its forecast.",
                modifier = contentModifier,
            )

            is ForecastUiState.Error -> ErrorState(
                message = "Couldn't load the forecast for ${uiState.city.name}. " +
                    "Check your connection and try again.",
                onRetry = onRetry,
                modifier = contentModifier,
            )

            is ForecastUiState.Success -> ForecastContent(
                uiState = uiState,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun ForecastContent(
    uiState: ForecastUiState.Success,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("forecast_content"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CurrentWeatherCard(current = uiState.current) }

        item {
            Text(
                text = "Next hours",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item { HourlyRow(hourly = uiState.hourly) }

        item {
            Text(
                text = "7-day forecast",
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
                text = current.condition.label,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Feels like ${current.feelsLike.roundToInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Humidity ${current.humidity}%",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Wind ${current.windSpeed.roundToInt()} km/h",
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
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(56.dp),
            )
            WeatherConditionIcon(
                condition = day.condition,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = day.condition.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${day.minTemperature.roundToInt()}° / ${day.maxTemperature.roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

private val HOUR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
