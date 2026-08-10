package com.allenljf.weatherforecast.core.network.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.allenljf.weatherforecast.core.network.api.AirQualityApi
import com.allenljf.weatherforecast.core.network.api.ForecastApi
import com.allenljf.weatherforecast.core.network.api.GeocodingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
annotation class AirQualityRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AirQualityBaseUrl

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

    @Provides
    @AirQualityBaseUrl
    fun provideAirQualityBaseUrl(): String = "https://air-quality-api.open-meteo.com/"
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        return OkHttpClient.Builder()
            .apply {
                if (isDebuggable) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        },
                    )
                }
            }
            .build()
    }

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
    @AirQualityRetrofit
    fun provideAirQualityRetrofit(
        json: Json,
        okHttpClient: OkHttpClient,
        @AirQualityBaseUrl baseUrl: String,
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

    @Provides
    @Singleton
    fun provideAirQualityApi(@AirQualityRetrofit retrofit: Retrofit): AirQualityApi =
        retrofit.create(AirQualityApi::class.java)
}
