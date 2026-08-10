package com.allenljf.weatherforecast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allenljf.weatherforecast.core.database.dao.CityDao
import com.allenljf.weatherforecast.core.database.dao.ForecastCacheDao
import com.allenljf.weatherforecast.core.database.entity.CachedForecastEntity
import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity

@Database(
    entities = [SavedCityEntity::class, CachedForecastEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    abstract fun forecastCacheDao(): ForecastCacheDao

    companion object {

        /**
         * Adds the offline forecast cache table. Purely additive, so saved cities survive the
         * upgrade — this is why the database is migrated instead of destructively recreated.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cached_forecasts` (" +
                        "`cityId` INTEGER NOT NULL, " +
                        "`payload` TEXT NOT NULL, " +
                        "`fetchedAtMillis` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`cityId`))",
                )
            }
        }
    }
}
