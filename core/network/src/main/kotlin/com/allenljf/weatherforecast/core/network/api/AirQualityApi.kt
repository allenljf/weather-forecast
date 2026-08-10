package com.allenljf.weatherforecast.core.network.api

import com.allenljf.weatherforecast.core.network.model.AirQualityResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApi {

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = DEFAULT_CURRENT_FIELDS,
        @Query("timezone") timezone: String = "auto",
    ): AirQualityResponseDto

    companion object {
        const val DEFAULT_CURRENT_FIELDS = "european_aqi,pm2_5,pm10"
    }
}
