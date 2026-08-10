package com.allenljf.weatherforecast.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allenljf.weatherforecast.core.database.WeatherDatabase
import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class CityDaoTest {

    private lateinit var db: WeatherDatabase
    private lateinit var dao: CityDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cityDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeAll_returnsCitiesOrderedByPositionAscending() = runTest {
        dao.insert(city(id = 3L, name = "London", position = 2))
        dao.insert(city(id = 1L, name = "Taipei", position = 0))
        dao.insert(city(id = 2L, name = "Tokyo", position = 1))

        val cities = dao.observeAll().first()

        assertEquals(listOf("Taipei", "Tokyo", "London"), cities.map(SavedCityEntity::name))
        assertEquals(listOf(0, 1, 2), cities.map(SavedCityEntity::position))
    }

    @Test
    fun insert_withSameId_replacesExistingRow() = runTest {
        dao.insert(city(id = 1L, name = "Taipei", position = 0))
        dao.insert(city(id = 1L, name = "Taipei City", position = 3))

        val cities = dao.observeAll().first()

        assertEquals(1, cities.size)
        assertEquals("Taipei City", cities.single().name)
        assertEquals(3, cities.single().position)
    }

    @Test
    fun deleteById_removesOnlyThatCity() = runTest {
        dao.insert(city(id = 1L, name = "Taipei", position = 0))
        dao.insert(city(id = 2L, name = "Tokyo", position = 1))

        dao.deleteById(1L)

        val cities = dao.observeAll().first()
        assertEquals(listOf(2L), cities.map(SavedCityEntity::id))
    }

    @Test
    fun maxPosition_returnsNullWhenTableIsEmpty() = runTest {
        assertNull(dao.maxPosition())
    }

    @Test
    fun maxPosition_returnsLargestPosition() = runTest {
        dao.insert(city(id = 1L, name = "Taipei", position = 0))
        dao.insert(city(id = 2L, name = "Tokyo", position = 7))
        dao.insert(city(id = 3L, name = "London", position = 4))

        assertEquals(7, dao.maxPosition())
    }

    @Test
    fun countAll_returnsNumberOfRows() = runTest {
        assertEquals(0, dao.countAll())

        dao.insert(city(id = 1L, name = "Taipei", position = 0))
        dao.insert(city(id = 2L, name = "Tokyo", position = 1))

        assertEquals(2, dao.countAll())
    }

    private fun city(
        id: Long,
        name: String,
        position: Int,
        country: String = "Country",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
    ) = SavedCityEntity(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        position = position,
    )
}
