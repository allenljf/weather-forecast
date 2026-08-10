package com.allenljf.weatherforecast.core.data.mapper

import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.network.model.GeocodingResultDto
import org.junit.Assert.assertEquals
import org.junit.Test

class CityMappersTest {

    @Test
    fun `maps entity to domain city`() {
        val entity = SavedCityEntity(
            id = 1668341,
            name = "Taipei",
            country = "Taiwan",
            latitude = 25.03,
            longitude = 121.56,
            position = 3,
        )

        val city = entity.toDomain()

        assertEquals(
            City(id = 1668341, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56),
            city,
        )
    }

    @Test
    fun `maps domain city to entity with given position`() {
        val city = City(id = 1668341, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)

        val entity = city.toEntity(position = 5)

        assertEquals(
            SavedCityEntity(
                id = 1668341,
                name = "Taipei",
                country = "Taiwan",
                latitude = 25.03,
                longitude = 121.56,
                position = 5,
            ),
            entity,
        )
    }

    @Test
    fun `maps geocoding result to domain city`() {
        val dto = GeocodingResultDto(
            id = 1850147,
            name = "Tokyo",
            latitude = 35.68,
            longitude = 139.69,
            country = "Japan",
        )

        val city = dto.toDomain()

        assertEquals(
            City(id = 1850147, name = "Tokyo", country = "Japan", latitude = 35.68, longitude = 139.69),
            city,
        )
    }

    @Test
    fun `maps null country to empty string`() {
        val dto = GeocodingResultDto(
            id = 42,
            name = "Nowhere",
            latitude = 0.0,
            longitude = 0.0,
            country = null,
        )

        assertEquals("", dto.toDomain().country)
    }
}
