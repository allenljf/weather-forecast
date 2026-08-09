package com.allenljf.weatherforecast.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allenljf.weatherforecast.core.database.WeatherDatabase
import com.allenljf.weatherforecast.core.database.dao.CityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Seeds the database with five default cities the first time it is created.
 *
 * Ids are GeoNames ids, matching the `id` field returned by the Open-Meteo geocoding API,
 * so searching for and re-adding one of these cities replaces instead of duplicating it.
 */
object DefaultCitiesSeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL(
            """
            INSERT INTO saved_cities (id, name, country, latitude, longitude, position) VALUES
                (1668341, 'Taipei', 'Taiwan', 25.0478, 121.5319, 0),
                (1850147, 'Tokyo', 'Japan', 35.6895, 139.6917, 1),
                (2643743, 'London', 'United Kingdom', 51.5085, -0.1257, 2),
                (2988507, 'Paris', 'France', 48.8534, 2.3488, 3),
                (5128581, 'New York', 'United States', 40.7143, -74.006, 4)
            """.trimIndent(),
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase =
        Room.databaseBuilder(context, WeatherDatabase::class.java, "weather.db")
            .addCallback(DefaultCitiesSeedCallback)
            .build()

    @Provides
    fun provideCityDao(database: WeatherDatabase): CityDao = database.cityDao()
}
