package com.allenljf.weatherforecast.core.database.di

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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class DefaultCitiesSeedCallbackTest {

    private lateinit var db: WeatherDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .addCallback(DefaultCitiesSeedCallback)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun onCreate_seedsFiveDefaultCities() = runTest {
        assertEquals(5, db.cityDao().countAll())
    }

    @Test
    fun onCreate_seedsCitiesInExpectedOrderWithTaipeiFirst() = runTest {
        val cities = db.cityDao().observeAll().first()

        assertEquals(
            listOf("Taipei", "Tokyo", "London", "Paris", "New York"),
            cities.map(SavedCityEntity::name),
        )
        assertEquals(listOf(0, 1, 2, 3, 4), cities.map(SavedCityEntity::position))

        val taipei = cities.first()
        assertEquals(1668341L, taipei.id)
        assertEquals("Taiwan", taipei.country)
        assertEquals(25.0478, taipei.latitude, 0.0001)
        assertEquals(121.5319, taipei.longitude, 0.0001)
    }

    @Test
    fun onCreate_seedsExpectedGeoNamesIds() = runTest {
        val cities = db.cityDao().observeAll().first()

        assertEquals(
            listOf(1668341L, 1850147L, 2643743L, 2988507L, 5128581L),
            cities.map(SavedCityEntity::id),
        )
    }
}
