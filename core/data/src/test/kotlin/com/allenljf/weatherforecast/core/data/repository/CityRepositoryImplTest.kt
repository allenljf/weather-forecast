package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.database.dao.CityDao
import com.allenljf.weatherforecast.core.database.entity.SavedCityEntity
import com.allenljf.weatherforecast.core.datastore.SelectedCityDataSource
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.network.api.GeocodingApi
import com.allenljf.weatherforecast.core.network.model.GeocodingResponseDto
import com.allenljf.weatherforecast.core.network.model.GeocodingResultDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CityRepositoryImplTest {

    private val geocodingApi = mockk<GeocodingApi>()
    private val cityDao = mockk<CityDao>(relaxUnitFun = true)
    private val selectedCityDataSource = mockk<SelectedCityDataSource>(relaxUnitFun = true)
    private val repository = CityRepositoryImpl(geocodingApi, cityDao, selectedCityDataSource)

    private val taipeiEntity = SavedCityEntity(
        id = 1,
        name = "Taipei",
        country = "Taiwan",
        latitude = 25.03,
        longitude = 121.56,
        position = 0,
    )
    private val tokyoEntity = SavedCityEntity(
        id = 2,
        name = "Tokyo",
        country = "Japan",
        latitude = 35.68,
        longitude = 139.69,
        position = 1,
    )
    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)
    private val tokyo = City(id = 2, name = "Tokyo", country = "Japan", latitude = 35.68, longitude = 139.69)

    @Test
    fun `observeSavedCities maps entities to domain cities`() = runTest {
        every { cityDao.observeAll() } returns flowOf(listOf(taipeiEntity, tokyoEntity))

        assertEquals(listOf(taipei, tokyo), repository.observeSavedCities().first())
    }

    @Test
    fun `observeSelectedCity emits matching city`() = runTest {
        every { cityDao.observeAll() } returns flowOf(listOf(taipeiEntity, tokyoEntity))
        every { selectedCityDataSource.selectedCityId } returns flowOf(2L)

        assertEquals(tokyo, repository.observeSelectedCity().first())
    }

    @Test
    fun `observeSelectedCity emits null when id has no matching city`() = runTest {
        every { cityDao.observeAll() } returns flowOf(listOf(taipeiEntity))
        every { selectedCityDataSource.selectedCityId } returns flowOf(99L)

        assertNull(repository.observeSelectedCity().first())
    }

    @Test
    fun `observeSelectedCity emits null when nothing selected`() = runTest {
        every { cityDao.observeAll() } returns flowOf(listOf(taipeiEntity))
        every { selectedCityDataSource.selectedCityId } returns flowOf(null)

        assertNull(repository.observeSelectedCity().first())
    }

    @Test
    fun `searchCities maps results to domain cities`() = runTest {
        coEvery { geocodingApi.searchCities("tok") } returns GeocodingResponseDto(
            results = listOf(
                GeocodingResultDto(id = 2, name = "Tokyo", latitude = 35.68, longitude = 139.69, country = "Japan"),
            ),
        )

        assertEquals(AppResult.Success(listOf(tokyo)), repository.searchCities("tok"))
    }

    @Test
    fun `searchCities returns empty list when results are null`() = runTest {
        coEvery { geocodingApi.searchCities("nomatch") } returns GeocodingResponseDto(results = null)

        assertEquals(AppResult.Success(emptyList<City>()), repository.searchCities("nomatch"))
    }

    @Test
    fun `searchCities maps IOException to network error`() = runTest {
        coEvery { geocodingApi.searchCities(any()) } throws IOException("offline")

        assertEquals(AppResult.Error(AppError.Network), repository.searchCities("tok"))
    }

    @Test
    fun `addCity inserts at position 0 when table is empty`() = runTest {
        coEvery { cityDao.maxPosition() } returns null

        repository.addCity(taipei)

        coVerify { cityDao.insert(taipeiEntity.copy(position = 0)) }
    }

    @Test
    fun `addCity inserts after current max position`() = runTest {
        coEvery { cityDao.maxPosition() } returns 4

        repository.addCity(tokyo)

        coVerify { cityDao.insert(tokyoEntity.copy(position = 5)) }
    }

    @Test
    fun `removeCity delegates to dao`() = runTest {
        repository.removeCity(7L)

        coVerify { cityDao.deleteById(7L) }
    }

    @Test
    fun `selectCity delegates to data source`() = runTest {
        repository.selectCity(7L)

        coVerify { selectedCityDataSource.setSelectedCityId(7L) }
    }
}
