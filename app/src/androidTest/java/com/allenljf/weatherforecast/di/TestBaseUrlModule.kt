package com.allenljf.weatherforecast.di

import com.allenljf.weatherforecast.core.network.di.BaseUrlModule
import com.allenljf.weatherforecast.core.network.di.ForecastBaseUrl
import com.allenljf.weatherforecast.core.network.di.GeocodingBaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

/**
 * Points both APIs at the on-device MockWebServer used by E2E tests.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BaseUrlModule::class],
)
object TestBaseUrlModule {

    const val MOCK_SERVER_PORT = 8080

    @Provides
    @ForecastBaseUrl
    fun provideForecastBaseUrl(): String = "http://127.0.0.1:$MOCK_SERVER_PORT/"

    @Provides
    @GeocodingBaseUrl
    fun provideGeocodingBaseUrl(): String = "http://127.0.0.1:$MOCK_SERVER_PORT/"
}
