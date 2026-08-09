package com.allenljf.weatherforecast.core.domain.usecase

import com.allenljf.weatherforecast.core.domain.model.City
import com.allenljf.weatherforecast.core.domain.repository.CityRepository
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddCityUseCaseTest {

    private val cityRepository = mockk<CityRepository>(relaxed = true)
    private val useCase = AddCityUseCase(cityRepository)

    @Test
    fun `adding a city saves it and selects it`() = runTest {
        useCase(taipei)

        coVerifyOrder {
            cityRepository.addCity(taipei)
            cityRepository.selectCity(taipei.id)
        }
    }

    private val taipei = City(id = 1, name = "Taipei", country = "Taiwan", latitude = 25.03, longitude = 121.56)
}
