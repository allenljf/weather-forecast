package com.allenljf.weatherforecast.feature.cities

import androidx.lifecycle.SavedStateHandle
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
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

/** One-shot events the UI must react to (e.g. navigate) once work has completed. */
sealed interface CitiesEvent {
    /** A city was selected (directly or by adding); persistence has finished. */
    data object CitySelected : CitiesEvent
}

@HiltViewModel
class CitiesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeSavedCities: ObserveSavedCitiesUseCase,
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val searchCities: SearchCitiesUseCase,
    private val addCity: AddCityUseCase,
    private val removeCity: RemoveCityUseCase,
    private val selectCity: SelectCityUseCase,
) : ViewModel() {

    /** Survives process death so the search box is restored. */
    private val searchQuery: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")

    private val _events = Channel<CitiesEvent>(Channel.BUFFERED)
    val events: Flow<CitiesEvent> = _events.receiveAsFlow()

    /**
     * Declarative search pipeline: [transformLatest] cancels an in-flight search
     * as soon as a newer (debounced) query arrives, so stale results can never
     * overwrite newer state. [SearchState.query] records which query a state
     * belongs to; the combine below drops states from outdated queries.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val searchState: Flow<SearchState> = searchQuery
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .transformLatest { query ->
            if (query.length < SearchCitiesUseCase.MIN_QUERY_LENGTH) {
                emit(SearchState(query = query))
                return@transformLatest
            }
            emit(SearchState(query = query, isLoading = true))
            emit(
                when (val result = searchCities(query)) {
                    is AppResult.Success -> SearchState(query = query, results = result.data)
                    is AppResult.Error -> SearchState(query = query, failed = true)
                },
            )
        }
        .onStart { emit(SearchState()) }

    val uiState: StateFlow<CitiesUiState> = combine(
        observeSavedCities(),
        observeSelectedCity(),
        searchQuery,
        searchState,
    ) { savedCities, selectedCity, query, search ->
        val trimmed = query.trim()
        val searchActive = trimmed.length >= SearchCitiesUseCase.MIN_QUERY_LENGTH
        val isCurrentQuery = search.query == trimmed
        CitiesUiState(
            savedCities = savedCities,
            selectedCityId = selectedCity?.id,
            searchQuery = query,
            searchResults = if (isCurrentQuery) search.results else emptyList(),
            isSearching = searchActive && (!isCurrentQuery || search.isLoading),
            searchFailed = isCurrentQuery && search.failed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CitiesUiState(),
    )

    fun onSearchQueryChange(query: String) {
        savedStateHandle[KEY_SEARCH_QUERY] = query
    }

    fun onClearSearch() {
        savedStateHandle[KEY_SEARCH_QUERY] = ""
    }

    /**
     * Persists first, then emits [CitiesEvent.CitySelected] so navigation only
     * happens after the write completed — popping the back stack earlier would
     * cancel [viewModelScope] mid-write.
     */
    fun onAddCity(city: City) {
        viewModelScope.launch {
            addCity(city)
            onClearSearch()
            _events.send(CitiesEvent.CitySelected)
        }
    }

    fun onRemoveCity(cityId: Long) {
        viewModelScope.launch {
            removeCity(cityId)
        }
    }

    fun onSelectCity(cityId: Long) {
        viewModelScope.launch {
            selectCity(cityId)
            _events.send(CitiesEvent.CitySelected)
        }
    }

    private data class SearchState(
        val query: String = "",
        val results: List<City> = emptyList(),
        val isLoading: Boolean = false,
        val failed: Boolean = false,
    )

    companion object {
        private const val SEARCH_DEBOUNCE_MILLIS = 300L
        private const val KEY_SEARCH_QUERY = "search_query"
    }
}
