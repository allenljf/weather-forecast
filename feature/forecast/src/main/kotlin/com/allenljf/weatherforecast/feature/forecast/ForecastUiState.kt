package com.allenljf.weatherforecast.feature.forecast

import com.allenljf.weatherforecast.core.common.result.AppError
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

    data class Error(val city: City, val error: AppError) : ForecastUiState
}

/**
 * Everything the forecast screen renders. [banner] is shown on top of whatever
 * [forecast] currently is, so a failed refresh can be surfaced without throwing
 * away already-visible data.
 */
data class ForecastScreenState(
    val forecast: ForecastUiState = ForecastUiState.Loading,
    val isRefreshing: Boolean = false,
    val banner: ForecastBanner? = null,
)

/**
 * Persistent top banner. It cannot be dismissed by the user — dismissing would
 * remove the only affordance to retry — and disappears only when a load succeeds.
 */
sealed interface ForecastBanner {
    /** Device reports no connectivity; a retry is attempted automatically once back online. */
    data object Offline : ForecastBanner

    /** A request failed while the device appears to be online. */
    data class LoadFailed(val error: AppError) : ForecastBanner
}
