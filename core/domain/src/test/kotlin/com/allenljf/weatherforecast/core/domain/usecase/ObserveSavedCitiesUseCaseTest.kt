package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveSavedCitiesUseCaseTest {

    private val cityRepository = mockk<CityRepository>()
    private val useCase = ObserveSavedCitiesUseCase(cityRepository)

    @Test
    fun `emits saved cities from repository`() = runTest {
        val cities = listOf(
            City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56),
        )
        every { cityRepository.observeSavedCities() } returns flowOf(cities)

        assertEquals(cities, useCase().first())
    }
}
