package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import javax.inject.Inject

class SelectCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(cityId: Long) {
        cityRepository.selectCity(cityId)
    }
}
