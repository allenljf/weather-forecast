package com.allenljf.weatherforecast.feature.forecast

import app.cash.turbine.test
import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.model.AppLanguage
import com.allenljf.weatherforecast.core.domain.usecase.GetForecastUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveAppLanguageUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SetAppLanguageUseCase
import com.allenljf.weatherforecast.core.testing.data.TestData
import com.allenljf.weatherforecast.core.testing.network.FakeNetworkMonitor
import com.allenljf.weatherforecast.core.testing.repository.FakeAppLanguageRepository
import com.allenljf.weatherforecast.core.testing.repository.FakeCityRepository
import com.allenljf.weatherforecast.core.testing.repository.FakeForecastRepository
import com.allenljf.weatherforecast.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val cityRepository = FakeCityRepository()
    private val forecastRepository = FakeForecastRepository()
    private val networkMonitor = FakeNetworkMonitor()

    private val languageRepository = FakeAppLanguageRepository()

    private fun createViewModel() = ForecastViewModel(
        observeSelectedCity = ObserveSelectedCityUseCase(cityRepository),
        getForecast = GetForecastUseCase(forecastRepository),
        networkMonitor = networkMonitor,
        observeAppLanguage = ObserveAppLanguageUseCase(languageRepository),
        setAppLanguage = SetAppLanguageUseCase(languageRepository),
    )

    private fun seedSelectedCity() {
        cityRepository.savedCities.value = listOf(TestData.taipei, TestData.tokyo)
        cityRepository.selectedCityId.value = TestData.taipei.id
    }

    @Test
    fun `no saved cities shows NoCitySelected`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.forecast is ForecastUiState.NoCitySelected }
        }
    }

    @Test
    fun `selected city loads forecast into Success state`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it.forecast is ForecastUiState.Success }
            val forecast = state.forecast as ForecastUiState.Success
            assertEquals(TestData.taipei, forecast.city)
            assertEquals(7, forecast.daily.size)
            assertNull(state.banner)
        }
    }

    @Test
    fun `hourly forecast only contains upcoming hours capped at 24`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val forecast = awaitItemMatching { it.forecast is ForecastUiState.Success }
                .forecast as ForecastUiState.Success
            assertTrue(forecast.hourly.isNotEmpty())
            assertTrue(forecast.hourly.size <= 24)
            assertTrue(forecast.hourly.all { !it.time.isBefore(forecast.current.time) })
        }
    }

    @Test
    fun `failed load shows Error with city`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val error = awaitItemMatching { it.forecast is ForecastUiState.Error }
                .forecast as ForecastUiState.Error
            assertEquals(TestData.taipei, error.city)
        }
    }

    @Test
    fun `retry after failure reloads forecast`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.forecast is ForecastUiState.Error }

            forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))
            viewModel.onRetry()
            runCurrent()

            awaitItemMatching { it.forecast is ForecastUiState.Success }
        }
    }

    @Test
    fun `switching selected city reloads forecast`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast(temperature = 30.0)))
        forecastRepository.setForecast(TestData.tokyo.id, AppResult.Success(TestData.forecast(temperature = 20.0)))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching {
                (it.forecast as? ForecastUiState.Success)?.city == TestData.taipei
            }

            cityRepository.selectedCityId.value = TestData.tokyo.id
            runCurrent()

            val forecast = awaitItemMatching {
                (it.forecast as? ForecastUiState.Success)?.city == TestData.tokyo
            }.forecast as ForecastUiState.Success
            assertEquals(20.0, forecast.current.temperature, 0.0)
        }
    }

    // --- Offline banner & auto-recovery -------------------------------------

    @Test
    fun `offline device shows persistent offline banner`() = runTest {
        seedSelectedCity()
        networkMonitor.onlineState.value = false
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it.banner != null }
            assertEquals(ForecastBanner.Offline, state.banner)
        }
    }

    @Test
    fun `failure while online shows load failed banner with the error`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Server(503)))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it.banner != null }
            assertEquals(ForecastBanner.LoadFailed(AppError.Server(503)), state.banner)
        }
    }

    @Test
    fun `reconnecting automatically reloads and clears the banner`() = runTest {
        seedSelectedCity()
        networkMonitor.onlineState.value = false
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.banner == ForecastBanner.Offline }

            // Network comes back and the API starts succeeding.
            forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))
            networkMonitor.onlineState.value = true
            runCurrent()

            val state = awaitItemMatching { it.forecast is ForecastUiState.Success }
            assertNull(state.banner)
        }
    }

    @Test
    fun `going offline while data is visible keeps the data and shows the banner`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.forecast is ForecastUiState.Success }

            networkMonitor.onlineState.value = false
            runCurrent()

            val state = awaitItemMatching { it.banner == ForecastBanner.Offline }
            assertTrue("data must stay visible", state.forecast is ForecastUiState.Success)
        }
    }

    // --- Pull to refresh ----------------------------------------------------

    @Test
    fun `pull to refresh reloads without dropping the visible forecast`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast(temperature = 25.0)))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.forecast is ForecastUiState.Success }

            forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast(temperature = 31.0)))
            viewModel.onRefresh()
            runCurrent()

            val state = awaitItemMatching {
                ((it.forecast as? ForecastUiState.Success)?.current?.temperature) == 31.0
            }
            assertFalse(state.isRefreshing)
        }
    }

    @Test
    fun `failed refresh keeps existing data and shows banner`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast(temperature = 25.0)))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.forecast is ForecastUiState.Success }

            forecastRepository.setForecast(TestData.taipei.id, AppResult.Error(AppError.Network))
            viewModel.onRefresh()
            runCurrent()

            val state = awaitItemMatching { it.banner != null }
            val forecast = state.forecast as ForecastUiState.Success
            assertEquals(25.0, forecast.current.temperature, 0.0)
            assertFalse(state.isRefreshing)
        }
    }

    @Test
    fun `selecting a language persists it and surfaces it in state`() = runTest {
        seedSelectedCity()
        forecastRepository.setForecast(TestData.taipei.id, AppResult.Success(TestData.forecast()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.language == AppLanguage.ENGLISH }

            viewModel.onLanguageSelected(AppLanguage.TRADITIONAL_CHINESE)
            runCurrent()

            awaitItemMatching { it.language == AppLanguage.TRADITIONAL_CHINESE }
            assertEquals(AppLanguage.TRADITIONAL_CHINESE, languageRepository.language.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun app.cash.turbine.TurbineTestContext<ForecastScreenState>.awaitItemMatching(
        predicate: (ForecastScreenState) -> Boolean,
    ): ForecastScreenState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
