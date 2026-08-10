package com.allenljf.weatherforecast.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allenljf.weatherforecast.core.common.network.NetworkMonitor
import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import com.allenljf.weatherforecast.core.domain.usecase.GetAirQualityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.GetCachedForecastUseCase
import com.allenljf.weatherforecast.core.domain.usecase.GetForecastUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveAppLanguageUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveTemperatureUnitUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SetAppLanguageUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SetTemperatureUnitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ForecastViewModel @Inject constructor(
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val getForecast: GetForecastUseCase,
    private val getCachedForecast: GetCachedForecastUseCase,
    private val getAirQuality: GetAirQualityUseCase,
    private val networkMonitor: NetworkMonitor,
    observeAppLanguage: ObserveAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase,
    observeTemperatureUnit: ObserveTemperatureUnitUseCase,
    private val setTemperatureUnit: SetTemperatureUnitUseCase,
) : ViewModel() {

    /** Bumped by retry, pull-to-refresh, and by regaining connectivity. */
    private val reloadTrigger = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)
    private val airQuality = MutableStateFlow<AirQuality?>(null)
    private val isOnline = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Result of the latest load, kept separate so a failed refresh can retain data. */
    private val loadState = MutableStateFlow<LoadState>(LoadState.Loading)

    private val language = observeAppLanguage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.DEFAULT)
    private val temperatureUnit = observeTemperatureUnit()
        .stateIn(viewModelScope, SharingStarted.Eagerly, TemperatureUnit.DEFAULT)

    val uiState: StateFlow<ForecastScreenState> = combine(
        loadState,
        isRefreshing,
        isOnline,
        combine(language, temperatureUnit, ::Pair),
        airQuality,
    ) { load, refreshing, online, (language, unit), airQuality ->
        ForecastScreenState(
            forecast = load.toUiState(),
            isRefreshing = refreshing,
            banner = bannerFor(load, online),
            language = language,
            temperatureUnit = unit,
            airQuality = airQuality,
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
                    if (city == null) flowOf(LoadState.NoCity) else loadForecast(city)
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

    /**
     * Offline-first: emit the cached forecast (marked stale) before the network
     * call, so there is something on screen immediately and while offline.
     */
    private fun loadForecast(city: City) = flow {
        // Tracked locally rather than read back from loadState: emissions reach
        // the collector asynchronously, so loadState may still hold the old value.
        var previous = (loadState.value as? LoadState.Loaded)?.takeIf { it.city == city }

        if (previous == null) {
            val cached = getCachedForecast(city)
            if (cached == null) {
                emit(LoadState.Loading)
            } else {
                val fromCache = LoadState.Loaded(city, cached, isStale = true)
                previous = fromCache
                emit(fromCache)
            }
        }

        emit(
            when (val result = getForecast(city)) {
                is AppResult.Success -> LoadState.Loaded(city, result.data, isStale = false)
                is AppResult.Error -> LoadState.Failed(city, result.error, previous)
            },
        )
        isRefreshing.value = false

        loadAirQuality(city)
    }

    /** Air quality is supplementary: a failure leaves the forecast untouched. */
    private fun loadAirQuality(city: City) {
        viewModelScope.launch {
            airQuality.value = (getAirQuality(city) as? AppResult.Success)?.data
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch { setAppLanguage(language) }
    }

    fun onToggleTemperatureUnit() {
        viewModelScope.launch {
            setTemperatureUnit(
                when (temperatureUnit.value) {
                    TemperatureUnit.CELSIUS -> TemperatureUnit.FAHRENHEIT
                    TemperatureUnit.FAHRENHEIT -> TemperatureUnit.CELSIUS
                },
            )
        }
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
        is LoadState.Loaded -> toSuccessState()
        // A failed load keeps previously shown data (cached or fresh) on screen;
        // the banner explains why it isn't updating.
        is LoadState.Failed -> previous?.copy(isStale = true)?.toSuccessState()
            ?: ForecastUiState.Error(city, error)
    }

    private fun LoadState.Loaded.toSuccessState(): ForecastUiState.Success {
        val forecast = cached.forecast
        val today = forecast.today
        return ForecastUiState.Success(
            city = city,
            current = forecast.current,
            hourly = forecast.hourly
                .filter { !it.time.isBefore(forecast.current.time) }
                .take(UPCOMING_HOURS),
            daily = forecast.daily,
            fetchedAt = cached.fetchedAt,
            isStale = isStale,
            sunrise = today?.sunrise,
            sunset = today?.sunset,
        )
    }

    private sealed interface LoadState {
        data object Loading : LoadState
        data object NoCity : LoadState
        data class Loaded(
            val city: City,
            val cached: CachedForecast,
            val isStale: Boolean,
        ) : LoadState

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
