package com.allenljf.weatherforecast.feature.cities

import app.cash.turbine.test
import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import com.allenljf.weatherforecast.core.domain.usecase.AddCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSavedCitiesUseCase
import com.allenljf.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.RemoveCityUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SearchCitiesUseCase
import com.allenljf.weatherforecast.core.domain.usecase.SelectCityUseCase
import com.allenljf.weatherforecast.core.testing.data.TestData
import com.allenljf.weatherforecast.core.testing.repository.FakeCityRepository
import com.allenljf.weatherforecast.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val cityRepository = FakeCityRepository()

    private fun createViewModel() = CitiesViewModel(
        savedStateHandle = androidx.lifecycle.SavedStateHandle(),
        observeSavedCities = ObserveSavedCitiesUseCase(cityRepository),
        observeSelectedCity = ObserveSelectedCityUseCase(cityRepository),
        searchCities = SearchCitiesUseCase(cityRepository),
        addCity = AddCityUseCase(cityRepository),
        removeCity = RemoveCityUseCase(cityRepository),
        selectCity = SelectCityUseCase(cityRepository),
    )

    @Test
    fun `ui state exposes saved cities and selected city`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei, TestData.tokyo)
        cityRepository.selectedCityId.value = TestData.tokyo.id

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItemMatching { it.savedCities.isNotEmpty() }
            assertEquals(listOf(TestData.taipei, TestData.tokyo), state.savedCities)
            assertEquals(TestData.tokyo.id, state.selectedCityId)
        }
    }

    @Test
    fun `search query updates immediately and results arrive after debounce`() = runTest {
        cityRepository.searchResult = AppResult.Success(listOf(TestData.london))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onSearchQueryChange("Lon")
            val typing = awaitItemMatching { it.searchQuery == "Lon" }
            assertTrue(typing.searchResults.isEmpty())

            advanceTimeBy(400)
            val searched = awaitItemMatching { it.searchResults.isNotEmpty() }
            assertEquals(listOf(TestData.london), searched.searchResults)
        }
    }

    @Test
    fun `failed search flags error`() = runTest {
        cityRepository.searchResult = AppResult.Error(AppError.Network)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onSearchQueryChange("Lon")
            advanceTimeBy(400)
            val state = awaitItemMatching { it.searchFailed }
            assertTrue(state.searchResults.isEmpty())
        }
    }

    @Test
    fun `adding a city saves it, selects it and clears search`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            viewModel.onSearchQueryChange("Lon")
            advanceTimeBy(400)

            viewModel.onAddCity(TestData.london)
            runCurrent()

            val state = awaitItemMatching {
                it.savedCities.contains(TestData.london) &&
                    it.selectedCityId == TestData.london.id &&
                    it.searchQuery.isEmpty()
            }
            assertTrue(state.searchResults.isEmpty())
        }
    }

    @Test
    fun `removing a city deletes it from saved list`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei, TestData.tokyo)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItemMatching { it.savedCities.size == 2 }

            viewModel.onRemoveCity(TestData.tokyo.id)
            runCurrent()

            val state = awaitItemMatching { it.savedCities.size == 1 }
            assertEquals(listOf(TestData.taipei), state.savedCities)
        }
    }

    @Test
    fun `selecting a city updates selection`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei, TestData.tokyo)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onSelectCity(TestData.tokyo.id)
            runCurrent()

            val state = awaitItemMatching { it.selectedCityId == TestData.tokyo.id }
            assertEquals(TestData.tokyo.id, state.selectedCityId)
        }
    }

    @Test
    fun `clearing search resets query and results`() = runTest {
        cityRepository.searchResult = AppResult.Success(listOf(TestData.london))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onSearchQueryChange("Lon")
            advanceTimeBy(400)
            awaitItemMatching { it.searchResults.isNotEmpty() }

            viewModel.onClearSearch()

            val state = awaitItemMatching { it.searchQuery.isEmpty() }
            assertTrue(state.searchResults.isEmpty())
        }
    }

    @Test
    fun `stale results from a cancelled search are never shown`() = runTest {
        // First search is slow; a second query arrives while it is in flight.
        cityRepository.searchDelayMillis = 500
        cityRepository.searchResultsByQuery["Lon"] = AppResult.Success(listOf(TestData.london))
        cityRepository.searchResultsByQuery["Tok"] = AppResult.Success(listOf(TestData.tokyo))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onSearchQueryChange("Lon")
            advanceTimeBy(400) // debounce fired at 300ms; "Lon" search is now in flight

            viewModel.onSearchQueryChange("Tok")
            advanceTimeBy(2_000) // "Lon" must be cancelled; "Tok" completes

            var state = awaitItem()
            while (state.searchResults != listOf(TestData.tokyo)) {
                assertTrue(
                    "stale London results leaked into state: $state",
                    !state.searchResults.contains(TestData.london),
                )
                state = awaitItem()
            }
            assertEquals("Tok", state.searchQuery)
        }
    }

    @Test
    fun `adding a city emits CitySelected only after persistence completed`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAddCity(TestData.london)
            runCurrent()

            assertEquals(CitiesEvent.CitySelected, awaitItem())
            // By the time the event is observable, the write must be done.
            assertTrue(cityRepository.savedCities.value.contains(TestData.london))
            assertEquals(TestData.london.id, cityRepository.selectedCityId.value)
        }
    }

    @Test
    fun `selecting a city emits CitySelected after persistence completed`() = runTest {
        cityRepository.savedCities.value = listOf(TestData.taipei, TestData.tokyo)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onSelectCity(TestData.tokyo.id)
            runCurrent()

            assertEquals(CitiesEvent.CitySelected, awaitItem())
            assertEquals(TestData.tokyo.id, cityRepository.selectedCityId.value)
        }
    }

    private suspend fun app.cash.turbine.TurbineTestContext<CitiesUiState>.awaitItemMatching(
        predicate: (CitiesUiState) -> Boolean,
    ): CitiesUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
