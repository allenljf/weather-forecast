package com.allenljf.weatherforecast.feature.forecast

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.model.CurrentWeather
import com.allenljf.weatherforecast.core.domain.model.DailyForecast
import com.allenljf.weatherforecast.core.domain.model.HourlyForecast
import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import java.time.Instant
import java.time.LocalDateTime

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
        /** When this data was fetched, so the UI can show its age. */
        val fetchedAt: Instant,
        /** True while showing cached data that hasn't been refreshed this session. */
        val isStale: Boolean = false,
        /** Today's sunrise/sunset, taken from the first daily entry. */
        val sunrise: LocalDateTime? = null,
        val sunset: LocalDateTime? = null,
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
    val language: AppLanguage = AppLanguage.DEFAULT,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.DEFAULT,
    /** Null while loading or when the air quality request failed. */
    val airQuality: AirQuality? = null,
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
