package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.AirQualityRepository
import com.allenljf.weatherforecast.core.network.api.AirQualityApi
import javax.inject.Inject

class AirQualityRepositoryImpl @Inject constructor(
    private val airQualityApi: AirQualityApi,
) : AirQualityRepository {

    override suspend fun getAirQuality(city: City): AppResult<AirQuality> {
        val result = safeApiCall {
            airQualityApi.getAirQuality(latitude = city.latitude, longitude = city.longitude)
        }
        return when (result) {
            is AppResult.Error -> result
            is AppResult.Success -> {
                // The endpoint answers 200 with an empty payload for locations it
                // has no coverage for; treat a missing AQI as an error, not as zero.
                val aqi = result.data.current?.europeanAqi
                    ?: return AppResult.Error(AppError.Unknown("no air quality data"))
                AppResult.Success(
                    AirQuality(
                        europeanAqi = aqi,
                        pm2_5 = result.data.current?.pm25,
                        pm10 = result.data.current?.pm10,
                    ),
                )
            }
        }
    }
}
