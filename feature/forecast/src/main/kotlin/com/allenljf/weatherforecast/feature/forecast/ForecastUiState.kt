package com.allenljf.weatherforecast.feature.forecast

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast

sealed interface ForecastUiState {
    data object Loading : ForecastUiState

    /** No city is saved yet, so there is nothing to forecast. */
    data object NoCitySelected : ForecastUiState

    data class Success(
        val city: City,
        val current: CurrentWeather,
        /** Upcoming hours starting from the current observation time. */
        val hourly: List<HourlyForecast>,
        /** Seven-day forecast including today. */
        val daily: List<DailyForecast>,
    ) : ForecastUiState

    data class Error(val city: City) : ForecastUiState
}
