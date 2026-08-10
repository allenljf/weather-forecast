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
    fun bindUserPreferencesRepository(
        impl: com.allenljf.weatherforecast.core.data.repository.UserPreferencesRepositoryImpl,
    ): com.allenljf.weatherforecast.core.domain.repository.UserPreferencesRepository

    @Binds
    @Singleton
    fun bindAirQualityRepository(
        impl: com.allenljf.weatherforecast.core.data.repository.AirQualityRepositoryImpl,
    ): com.allenljf.weatherforecast.core.domain.repository.AirQualityRepository

    @Binds
    @Singleton
    fun bindAppLanguageRepository(
        impl: com.allenljf.weatherforecast.core.data.repository.AppLanguageRepositoryImpl,
    ): com.allenljf.weatherforecast.core.domain.repository.AppLanguageRepository
}

@dagger.Module
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
internal object ClockModule {
    @dagger.Provides
    fun provideClock(): java.time.Clock = java.time.Clock.systemUTC()
}
