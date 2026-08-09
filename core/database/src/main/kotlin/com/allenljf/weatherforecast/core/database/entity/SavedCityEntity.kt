package com.allenljf.weatherforecast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A city the user has saved for weather tracking.
 *
 * @property id GeoNames id as returned by the Open-Meteo geocoding API. Used as the
 * primary key (not auto-generated) so adding the same city twice cannot create duplicates.
 * @property position Insertion order used to keep the saved-cities list stable.
 */
@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val position: Int,
)
