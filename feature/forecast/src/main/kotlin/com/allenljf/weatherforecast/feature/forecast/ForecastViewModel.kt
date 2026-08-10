package com.allenljf.weatherforecast.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allenljf.weatherforecast.core.common.network.NetworkMonitor
import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.usecase.GetForecastUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveAppLanguageUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SetAppLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ForecastViewModel @Inject constructor(
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val getForecast: GetForecastUseCase,
    private val networkMonitor: NetworkMonitor,
    observeAppLanguage: ObserveAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase,
) : ViewModel() {

    /** Bumped by retry, pull-to-refresh, and by regaining connectivity. */
    private val reloadTrigger = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)
    private val isOnline = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Result of the latest load, kept separate so a failed refresh can retain data. */
    private val loadState = MutableStateFlow<LoadState>(LoadState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val language = observeAppLanguage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.DEFAULT)

    val uiState: StateFlow<ForecastScreenState> = combine(
        loadState,
        isRefreshing,
        isOnline,
        language,
    ) { load, refreshing, online, language ->
        ForecastScreenState(
            forecast = load.toUiState(),
            isRefreshing = refreshing,
            banner = bannerFor(load, online),
            language = language,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ForecastScreenState(),
    )

    private val selectedCity = observeSelectedCity().distinctUntilChanged()

    init {
        observeLoads()
        autoRecoverWhenBackOnline()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeLoads() {
        viewModelScope.launch {
            combine(selectedCity, reloadTrigger) { city, _ -> city }
                .flatMapLatest { city ->
                    if (city == null) {
                        flowOf(LoadState.NoCity)
                    } else {
                        loadForecast(city)
                    }
                }
                .collect { loadState.value = it }
        }
    }

    /**
     * When connectivity returns and the last load failed, retry automatically so
     * the user doesn't have to press anything.
     */
    private fun autoRecoverWhenBackOnline() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .filter { online -> online }
                .collect {
                    if (loadState.value is LoadState.Failed) {
                        reloadTrigger.value += 1
                    }
                }
        }
    }

    private fun loadForecast(city: City) = kotlinx.coroutines.flow.flow {
        val previous = loadState.value as? LoadState.Loaded
        // Keep showing existing data while refreshing the same city.
        if (previous == null || previous.city != city) {
            emit(LoadState.Loading)
        }
        emit(
            when (val result = getForecast(city)) {
                is AppResult.Success -> LoadState.Loaded(city, result.data)
                is AppResult.Error -> LoadState.Failed(city, result.error, previous?.takeIf { it.city == city })
            },
        )
        isRefreshing.value = false
    }

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch { setAppLanguage(language) }
    }

    fun onRetry() {
        reloadTrigger.value += 1
    }

    fun onRefresh() {
        isRefreshing.value = true
        reloadTrigger.value += 1
    }

    private fun bannerFor(load: LoadState, online: Boolean): ForecastBanner? = when {
        !online -> ForecastBanner.Offline
        load is LoadState.Failed ->
            if (load.error == AppError.Network) ForecastBanner.Offline
            else ForecastBanner.LoadFailed(load.error)

        else -> null
    }

    private fun LoadState.toUiState(): ForecastUiState = when (this) {
        LoadState.Loading -> ForecastUiState.Loading
        LoadState.NoCity -> ForecastUiState.NoCitySelected
        is LoadState.Loaded -> forecast.toSuccessState(city)
        // A failed refresh keeps the previously loaded data on screen; the banner explains why.
        is LoadState.Failed -> previous?.let { it.forecast.toSuccessState(it.city) }
            ?: ForecastUiState.Error(city, error)
    }

    private fun WeatherForecast.toSuccessState(city: City): ForecastUiState.Success =
        ForecastUiState.Success(
            city = city,
            current = current,
            hourly = hourly
                .filter { !it.time.isBefore(current.time) }
                .take(UPCOMING_HOURS),
            daily = daily,
        )

    private sealed interface LoadState {
        data object Loading : LoadState
        data object NoCity : LoadState
        data class Loaded(val city: City, val forecast: WeatherForecast) : LoadState
        data class Failed(
            val city: City,
            val error: AppError,
            /** Data shown before this failure, if any, so it can stay visible. */
            val previous: Loaded?,
        ) : LoadState
    }

    companion object {
        private const val UPCOMING_HOURS = 24
    }
}
