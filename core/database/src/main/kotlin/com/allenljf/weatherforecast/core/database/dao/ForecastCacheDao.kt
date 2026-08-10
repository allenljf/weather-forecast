package com.allenljf.weatherforecast.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.allenljf.weatherforecast.core.database.entity.CachedForecastEntity

@Dao
interface ForecastCacheDao {

    @Query("SELECT * FROM cached_forecasts WHERE cityId = :cityId")
    suspend fun getByCityId(cityId: Long): CachedForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedForecastEntity)

    @Query("DELETE FROM cached_forecasts WHERE cityId = :cityId")
    suspend fun deleteByCityId(cityId: Long)

    @Query("DELETE FROM cached_forecasts")
    suspend fun clear()
}
