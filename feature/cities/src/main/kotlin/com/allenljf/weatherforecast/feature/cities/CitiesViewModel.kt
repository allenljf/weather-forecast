package com.allenljf.weatherforecast.feature.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.usecase.AddCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSavedCitiesUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.RemoveCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SearchCitiesUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SelectCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitiesViewModel @Inject constructor(
    observeSavedCities: ObserveSavedCitiesUseCase,
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val searchCities: SearchCitiesUseCase,
    private val addCity: AddCityUseCase,
    private val removeCity: RemoveCityUseCase,
    private val selectCity: SelectCityUseCase,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val searchState = MutableStateFlow(SearchState())

    val uiState: StateFlow<CitiesUiState> = combine(
        observeSavedCities(),
        observeSelectedCity(),
        searchQuery,
        searchState,
    ) { savedCities, selectedCity, query, search ->
        CitiesUiState(
            savedCities = savedCities,
            selectedCityId = selectedCity?.id,
            searchQuery = query,
            searchResults = search.results,
            isSearching = search.isLoading,
            searchFailed = search.failed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CitiesUiState(),
    )

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .collect { query ->
                    if (query.length < SearchCitiesUseCase.MIN_QUERY_LENGTH) {
                        searchState.value = SearchState()
                        return@collect
                    }
                    searchState.value = SearchState(isLoading = true)
                    searchState.value = when (val result = searchCities(query)) {
                        is AppResult.Success -> SearchState(results = result.data)
                        is AppResult.Error -> SearchState(failed = true)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onClearSearch() {
        searchQuery.value = ""
        searchState.value = SearchState()
    }

    fun onAddCity(city: City) {
        viewModelScope.launch {
            addCity.invoke(city)
            onClearSearch()
        }
    }

    fun onRemoveCity(cityId: Long) {
        viewModelScope.launch {
            removeCity.invoke(cityId)
        }
    }

    fun onSelectCity(cityId: Long) {
        viewModelScope.launch {
            selectCity.invoke(cityId)
        }
    }

    private data class SearchState(
        val results: List<City> = emptyList(),
        val isLoading: Boolean = false,
        val failed: Boolean = false,
    )

    companion object {
        private const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
