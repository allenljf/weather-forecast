package com.allenljf.weatherforecast.core.domain.repository

import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface AppLanguageRepository {
    fun observeLanguage(): Flow<AppLanguage>

    suspend fun setLanguage(language: AppLanguage)
}
