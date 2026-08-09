package com.allenljf.weatherforecast.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM saved_cities ORDER BY position ASC")
    fun observeAll(): Flow<List<SavedCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: SavedCityEntity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT MAX(position) FROM saved_cities")
    suspend fun maxPosition(): Int?

    @Query("SELECT COUNT(*) FROM saved_cities")
    suspend fun countAll(): Int
}
