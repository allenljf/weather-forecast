package com.allenljf.weatherforecast.core.data.di

import com.allenljf.weatherforecast.core.data.repository.CityRepositoryImpl
import com.allenljf.weatherforecast.core.data.repository.ForecastRepositoryImpl
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import com.allenljf.weatherforecast.core.domain.repository.ForecastRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    @Singleton
    fun bindForecastRepository(impl: ForecastRepositoryImpl): ForecastRepository

    @Binds
    @Singleton
    fun bindCityRepository(impl: CityRepositoryImpl): CityRepository

    @Binds
    @Singleton
    fun bindAppLanguageRepository(
        impl: com.allenljf.weatherforecast.core.data.repository.AppLanguageRepositoryImpl,
    ): com.allenljf.weatherforecast.core.domain.repository.AppLanguageRepository
}
