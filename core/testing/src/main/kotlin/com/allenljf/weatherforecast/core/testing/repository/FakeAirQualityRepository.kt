package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.AirQualityRepository

class FakeAirQualityRepository : AirQualityRepository {
    var result: AppResult<AirQuality> = AppResult.Error(AppError.Unknown("not stubbed"))

    override suspend fun getAirQuality(city: City): AppResult<AirQuality> = result
}
