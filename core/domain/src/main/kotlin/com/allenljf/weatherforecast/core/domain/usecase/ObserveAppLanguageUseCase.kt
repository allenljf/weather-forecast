package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.repository.AppLanguageRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAppLanguageUseCase @Inject constructor(
    private val repository: AppLanguageRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeLanguage()
}
