package com.allenljf.weatherforecast.core.testing.repository

import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.repository.AppLanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppLanguageRepository : AppLanguageRepository {
    val language = MutableStateFlow(AppLanguage.DEFAULT)

    override fun observeLanguage(): Flow<AppLanguage> = language

    override suspend fun setLanguage(language: AppLanguage) {
        this.language.value = language
    }
}
