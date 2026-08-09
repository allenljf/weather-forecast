package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchCitiesUseCaseTest {

    private val cityRepository = mockk<CityRepository>()
    private val useCase = SearchCitiesUseCase(cityRepository)

    @Test
    fun `blank query returns empty result without calling repository`() = runTest {
        val result = useCase("   ")

        assertEquals(AppResult.Success(emptyList<City>()), result)
        coVerify(exactly = 0) { cityRepository.searchCities(any()) }
    }

    @Test
    fun `query shorter than minimum returns empty result without calling repository`() = runTest {
        val result = useCase("t")

        assertEquals(AppResult.Success(emptyList<City>()), result)
        coVerify(exactly = 0) { cityRepository.searchCities(any()) }
    }

    @Test
    fun `valid query delegates to repository with trimmed input`() = runTest {
        val cities = listOf(taipei)
        coEvery { cityRepository.searchCities("Taipei") } returns AppResult.Success(cities)

        val result = useCase("  Taipei  ")

        assertEquals(AppResult.Success(cities), result)
        coVerify(exactly = 1) { cityRepository.searchCities("Taipei") }
    }

    @Test
    fun `repository error is propagated`() = runTest {
        val error = AppResult.Error(com.allenljf.weatherforecast.core.common.result.AppError.Network)
        coEvery { cityRepository.searchCities("Tokyo") } returns error

        val result = useCase("Tokyo")

        assertEquals(error, result)
    }

    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)
}
