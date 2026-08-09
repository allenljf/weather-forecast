package com.allenljf.weatherforecast.feature.forecast

import app.cash.turbine.test
import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.usecase.GetForecastUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.allenljf.weatherforecast.core.testing.data.TestData
import com.allenljf.weatherforecast.core.testing.repository.FakeCityRepository
import com.allenljf.weatherforecast.core.testing.repository.FakeForecastRepository
import com.allenljf.weatherforecast.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val cityRepository = FakeCityRepository()
    private val forecastRepository = FakeForecastRepository()

    private fun createViewModel() = ForecastViewModel(
        observeSelectedCity = ObserveSelectedCityUseCase(cityRepository),
        getForecast = GetForecastUseCase(forecastRepository),
    )

    @Test
    fun `no saved cities shows NoCitySelected`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it is ForecastUiState.NoCitySelected }
        }
    }

    @Test
    fun `selected city loads forecast into Success state`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei)
        cityRepository.selectedCityId.value = TestData.taipei.id
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it is ForecastUiState.Success } as ForecastUiState.Success
            assertEquals(TestData.taipei, state.city)
            assertEquals(TestData.forecast().current, state.current)
            assertEquals(7, state.daily.size)
        }
    }

    @Test
    fun `hourly forecast only contains upcoming hours capped at 24`() = runTest {
        // TestData.forecast() has 24 hourly entries for hours 0..23 and current time 12:00.
        cityRepository.savedCities.value = listOf(TestData.taipei)
        cityRepository.selectedCityId.value = TestData.taipei.id
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it is ForecastUiState.Success } as ForecastUiState.Success
            assertTrue(state.hourly.isNotEmpty())
            assertTrue(state.hourly.size <= 24)
            assertTrue(state.hourly.all { !it.time.isBefore(state.current.time) })
        }
    }

    @Test
    fun `failed load shows Error with city`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei)
        cityRepository.selectedCityId.value = TestData.taipei.id
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it is ForecastUiState.Error } as ForecastUiState.Error
            assertEquals(TestData.taipei, state.city)
        }
    }

    @Test
    fun `retry after failure reloads forecast`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei)
        cityRepository.selectedCityId.value = TestData.taipei.id
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it is ForecastUiState.Error }

            forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))
            viewModel.onRetry()
            runCurrent()

            awaitItemMatching { it is ForecastUiState.Success }
        }
    }

    @Test
    fun `switching selected city reloads forecast`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei, TestData.tokyo)
        cityRepository.selectedCityId.value = TestData.taipei.id
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast(temperature = 30.0)))
        forecastRepository.setForecast(TestData.tokyo.id, AppResult.Success(TestData.forecast(temperature = 20.0)))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val first = awaitItemMatching { it is ForecastUiState.Success } as ForecastUiState.Success
            assertEquals(TestData.taipei, first.city)

            cityRepository.selectedCityId.value = TestData.tokyo.id
            runCurrent()

            val second = awaitItemMatching {
                it is ForecastUiState.Success && it.city == TestData.tokyo
            } as ForecastUiState.Success
            assertEquals(20.0, second.current.temperature, 0.0)
        }
    }

    private suspend fun app.cash.turbine.TurbineTestContext<ForecastUiState>.awaitItemMatching(
        predicate: (ForecastUiState) -> Boolean,
    ): ForecastUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
