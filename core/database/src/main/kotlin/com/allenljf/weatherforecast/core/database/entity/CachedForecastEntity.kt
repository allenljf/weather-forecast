package com.allenljf.weatherforecast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An offline copy of the most recent forecast fetched for a city.
 *
 * @property cityId GeoNames id of the city, matching [SavedCityEntity.id]. One cached
 * forecast per city, so re-fetching replaces instead of accumulating rows.
 * @property payload Serialized WeatherForecast payload; the data layer owns the format.
 * @property fetchedAtMillis Epoch millis when this was fetched.
 */
@Entity(tableName = "cached_forecasts")
data class CachedForecastEntity(
    @PrimaryKey val cityId: Long,
    val payload: String,
    val fetchedAtMillis: Long,
)
