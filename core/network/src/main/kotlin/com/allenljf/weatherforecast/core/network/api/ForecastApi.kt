package com.allenljf.weatherforecast.core.network.api

import com.allenljf.weatherforecast.core.network.model.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = DEFAULT_CURRENT_FIELDS,
        @Query("hourly") hourly: String = DEFAULT_HOURLY_FIELDS,
        @Query("daily") daily: String = DEFAULT_DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
    ): ForecastResponseDto

    companion object {
        const val DEFAULT_CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m"
        const val DEFAULT_HOURLY_FIELDS = "temperature_2m,weather_code"
        const val DEFAULT_DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min"
    }
}
