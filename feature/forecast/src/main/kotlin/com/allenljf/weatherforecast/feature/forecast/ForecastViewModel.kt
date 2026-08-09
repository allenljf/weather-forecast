package com.allenljf.weatherforecast.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.WeatherForecast
import com.allenljf.weatherforecast.core.domain.usecase.GetForecastUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    observeSelectedCity: ObserveSelectedCityUseCase,
    private val getForecast: GetForecastUseCase,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ForecastUiState> = combine(
        observeSelectedCity().distinctUntilChanged(),
        retryTrigger,
    ) { city, _ -> city }
        .flatMapLatest { city -> loadForecast(city) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ForecastUiState.Loading,
        )

    private fun loadForecast(city: City?) = flow {
        if (city == null) {
            emit(ForecastUiState.NoCitySelected)
            return@flow
        }
        emit(ForecastUiState.Loading)
        emit(
            when (val result = getForecast(city)) {
                is AppResult.Success -> result.data.toSuccessState(city)
                is AppResult.Error -> ForecastUiState.Error(city, result.error)
            },
        )
    }

    fun onRetry() {
        retryTrigger.value += 1
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

    companion object {
        private const val UPCOMING_HOURS = 24
    }
}
