package com.allenljf.weatherforecast.core.network.di

import com.allenljf.weatherforecast.core.network.api.ForecastApi
import com.allenljf.weatherforecast.core.network.api.GeocodingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForecastRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForecastBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingBaseUrl

/**
 * Base URLs live in their own module so instrumentation tests can replace
 * them (via @TestInstallIn) with a local MockWebServer address.
 */
@Module
@InstallIn(SingletonComponent::class)
object BaseUrlModule {

    @Provides
    @ForecastBaseUrl
    fun provideForecastBaseUrl(): String = "https://api.open-meteo.com/"

    @Provides
    @GeocodingBaseUrl
    fun provideGeocodingBaseUrl(): String = "https://geocoding-api.open-meteo.com/"
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
            .build()

    @Provides
    @Singleton
    @ForecastRetrofit
    fun provideForecastRetrofit(
        json: Json,
        okHttpClient: OkHttpClient,
        @ForecastBaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(
        json: Json,
        okHttpClient: OkHttpClient,
        @GeocodingBaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideForecastApi(@ForecastRetrofit retrofit: Retrofit): ForecastApi =
        retrofit.create(ForecastApi::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)
}
