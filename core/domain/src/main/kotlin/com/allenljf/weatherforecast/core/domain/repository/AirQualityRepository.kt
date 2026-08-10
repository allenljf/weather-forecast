package com.allenljf.weatherforecast.core.domain.repository

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.City

interface AirQualityRepository {
    suspend fun getAirQuality(city: City): AppResult<AirQuality>
}
