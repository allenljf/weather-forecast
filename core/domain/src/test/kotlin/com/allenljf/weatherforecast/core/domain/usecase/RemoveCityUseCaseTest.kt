package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RemoveCityUseCaseTest {

    private val cityRepository = mockk<CityRepository>(relaxed = true)
    private val useCase = RemoveCityUseCase(cityRepository)

    @Test
    fun `removing an unselected city only removes it`() = runTest {
        every { cityRepository.observeSelectedCity() } returns flowOf(taipei)
        every { cityRepository.observeSavedCities() } returns flowOf(listOf(taipei, tokyo))

        useCase(tokyo.id)

        coVerify(exactly = 1) { cityRepository.removeCity(tokyo.id) }
        coVerify(exactly = 0) { cityRepository.selectCity(any()) }
    }

    @Test
    fun `removing the selected city moves selection to first remaining city`() = runTest {
        every { cityRepository.observeSelectedCity() } returns flowOf(taipei)
        every { cityRepository.observeSavedCities() } returns flowOf(listOf(taipei, tokyo))

        useCase(taipei.id)

        coVerify(exactly = 1) { cityRepository.removeCity(taipei.id) }
        coVerify(exactly = 1) { cityRepository.selectCity(tokyo.id) }
    }

    @Test
    fun `removing the selected last city does not reselect`() = runTest {
        every { cityRepository.observeSelectedCity() } returns flowOf(taipei)
        every { cityRepository.observeSavedCities() } returns flowOf(listOf(taipei))

        useCase(taipei.id)

        coVerify(exactly = 1) { cityRepository.removeCity(taipei.id) }
        coVerify(exactly = 0) { cityRepository.selectCity(any()) }
    }

    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)
    private val tokyo = City(id = 2, name = "Tokyo", country = "Japan", latitude = 35.68, longitude = 139.69)
}
