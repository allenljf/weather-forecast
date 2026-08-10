package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.TemperatureUnit
import com.allenljf.weatherforecast.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetTemperatureUnitUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(unit: TemperatureUnit) = repository.setTemperatureUnit(unit)
}
