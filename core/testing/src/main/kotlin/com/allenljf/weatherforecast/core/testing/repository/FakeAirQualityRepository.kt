package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AirQuality
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.AirQualityRepository
import kotlinx.coroutines.delay

class FakeAirQualityRepository : AirQualityRepository {
    var result: AppResult<AirQuality> = AppResult.Error(AppError.Unknown("not stubbed"))

    /** Per-city results, for tests that switch cities mid-flight. */
    val resultsByCityId = mutableMapOf<Long, AppResult<AirQuality>>()

    /** Simulated latency, on the test scheduler's virtual clock. */
    var delayMillis: Long = 0

    override suspend fun getAirQuality(city: City): AppResult<AirQuality> {
        if (delayMillis > 0) delay(delayMillis)
        return resultsByCityId[city.id] ?: result
    }
}
