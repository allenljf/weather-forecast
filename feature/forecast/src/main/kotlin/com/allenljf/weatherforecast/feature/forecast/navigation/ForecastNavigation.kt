package com.allenljf.weatherforecast.feature.forecast.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.allenljf.weatherforecast.feature.forecast.ForecastRoute

const val FORECAST_ROUTE = "forecast"

fun NavGraphBuilder.forecastScreen(
    onCitiesClick: () -> Unit,
) {
    composable(route = FORECAST_ROUTE) {
        ForecastRoute(onCitiesClick = onCitiesClick)
    }
}
