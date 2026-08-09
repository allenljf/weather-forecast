package com.allenljf.weatherforecast.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.allenljf.weatherforecast.core.database.WeatherDatabase
import com.allenljf.weatherforecast.core.database.dao.CityDao
import com.allenljf.weatherforecast.core.database.di.DatabaseModule
import com.allenljf.weatherforecast.core.database.di.DefaultCitiesSeedCallback
import com.allenljf.weatherforecast.core.datastore.di.DataStoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.UUID
import javax.inject.Singleton

/**
 * In-memory Room database per test component, seeded like production.
 * Avoids cross-test contamination without deleting files under a live instance.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase =
        Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .addCallback(DefaultCitiesSeedCallback)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideCityDao(database: WeatherDatabase): CityDao = database.cityDao()
}

/**
 * DataStore backed by a unique file per test component. DataStore forbids two
 * active instances on the same file within one process, so each Hilt test
 * component gets its own file.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class],
)
object TestDataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.cacheDir.resolve("test_prefs_${UUID.randomUUID()}.preferences_pb")
        }
}
