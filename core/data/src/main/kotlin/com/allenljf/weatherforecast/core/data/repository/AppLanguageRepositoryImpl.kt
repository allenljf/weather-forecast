package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.datastore.AppLanguageDataSource
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.repository.AppLanguageRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AppLanguageRepositoryImpl @Inject constructor(
    private val dataSource: AppLanguageDataSource,
) : AppLanguageRepository {

    override fun observeLanguage(): Flow<AppLanguage> = dataSource.language

    override suspend fun setLanguage(language: AppLanguage) = dataSource.setLanguage(language)
}
