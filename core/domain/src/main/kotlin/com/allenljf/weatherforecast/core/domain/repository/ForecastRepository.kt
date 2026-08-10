package com.allenljf.weatherforecast.core.domain.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.CachedForecast
import com.allenljf.weatherforecast.core.domain.model.City

interface ForecastRepository {
    /**
     * Last successfully fetched forecast for this city, or null if never fetched.
     * Lets the UI show something immediately (and while offline) before the
     * network call resolves.
     */
    suspend fun getCachedForecast(city: City): CachedForecast?

    /** Fetches a fresh forecast and caches it on success. */
    suspend fun refreshForecast(city: City): AppResult<CachedForecast>
}
