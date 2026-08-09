package com.allenljf.weatherforecast.feature.cities

import com.allenljf.weatherforecast.core.domain.model.City

data class CitiesUiState(
    val savedCities: List<City> = emptyList(),
    val selectedCityId: Long? = null,
    val searchQuery: String = "",
    val searchResults: List<City> = emptyList(),
    val isSearching: Boolean = false,
    val searchFailed: Boolean = false,
) {
    val isInSearchMode: Boolean get() = searchQuery.isNotBlank()
}
