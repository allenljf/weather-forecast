package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.AirQualityRepository
import javax.inject.Inject

class GetAirQualityUseCase @Inject constructor(
    private val airQualityRepository: AirQualityRepository,
) {
    suspend operator fun invoke(city: City): AppResult<AirQuality> =
        airQualityRepository.getAirQuality(city)
}
