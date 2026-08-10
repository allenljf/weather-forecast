package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.repository.AppLanguageRepository
import javax.inject.Inject

class SetAppLanguageUseCase @Inject constructor(
    private val repository: AppLanguageRepository,
) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
