package com.allenljf.weatherforecast.core.data.mapper

import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.network.model.GeocodingResultDto

fun SavedCityEntity.toDomain(): City = City(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude,
)

fun City.toEntity(position: Int): SavedCityEntity = SavedCityEntity(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude,
    position = position,
)

fun GeocodingResultDto.toDomain(): City = City(
    id = id,
    name = name,
    country = country.orEmpty(),
    latitude = latitude,
    longitude = longitude,
)
