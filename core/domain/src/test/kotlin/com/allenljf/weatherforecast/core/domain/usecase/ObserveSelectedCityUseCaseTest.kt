package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObserveSelectedCityUseCaseTest {

    private val cityRepository = mockk<CityRepository>()
    private val useCase = ObserveSelectedCityUseCase(cityRepository)

    @Test
    fun `emits explicitly selected city`() = runTest {
        every { cityRepository.observeSelectedCity() } returns flowOf(tokyo)
        every { cityRepository.observeSavedCities() } returns flowOf(listOf(taipei, tokyo))

        assertEquals(tokyo, useCase().first())
    }

    @Test
    fun `falls back to first saved city when nothing selected`() = runTest {
        every { cityRepository.observeSelectedCity() } returns flowOf(null)
        every { cityRepository.observeSavedCities() } returns flowOf(listOf(taipei, tokyo))

        assertEquals(taipei, useCase().first())
    }

    @Test
    fun `emits null when nothing selected and no saved cities`() = runTest {
        every { cityRepository.observeSelectedCity() } returns flowOf(null)
        every { cityRepository.observeSavedCities() } returns flowOf(emptyList())

        assertNull(useCase().first())
    }

    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)
    private val tokyo = City(id = 2, name = "Tokyo", country = "Japan", latitude = 35.68, longitude = 139.69)
}
