package com.allenljf.weatherforecast.feature.cities.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.allenljf.weatherforecast.feature.cities.CitiesRoute

const val CITIES_ROUTE = "cities"

fun NavController.navigateToCities() {
    navigate(CITIES_ROUTE)
}

fun NavGraphBuilder.citiesScreen(
    onBackClick: () -> Unit,
    onCitySelected: () -> Unit,
) {
    composable(route = CITIES_ROUTE) {
        CitiesRoute(
            onBackClick = onBackClick,
            onCitySelected = onCitySelected,
        )
    }
}
