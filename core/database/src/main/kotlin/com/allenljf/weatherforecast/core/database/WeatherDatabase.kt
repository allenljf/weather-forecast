package com.allenljf.weatherforecast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.allenljf.weatherforecast.core.database.dao.CityDao
import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity

@Database(
    entities = [SavedCityEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
}
