package com.allenljf.weatherforecast.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.allenljf.weatherforecast.feature.cities.navigation.citiesScreen
import com.allenljf.weatherforecast.feature.cities.navigation.navigateToCities
import com.allenljf.weatherforecast.feature.forecast.navigation.FORECAST_ROUTE
import com.allenljf.weatherforecast.feature.forecast.navigation.forecastScreen

@Composable
fun WeatherNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FORECAST_ROUTE,
    ) {
        forecastScreen(
            onCitiesClick = navController::navigateToCities,
        )
        citiesScreen(
            onBackClick = navController::popBackStack,
            onCitySelected = navController::popBackStack,
        )
    }
}
