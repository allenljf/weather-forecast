package com.allenljf.weatherforecast.e2e

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.allenljf.weatherforecast.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end tests against an on-device MockWebServer.
 * Storage is wiped before each test so the app starts from the seeded
 * default cities with Taipei as the fallback selection.
 */
@HiltAndroidTest
class WeatherForecastE2ETest : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val mockServerRule = MockWeatherServerRule()

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun displaysTodayAndWeeklyForecastForDefaultCity() = run {
        step("Today's forecast for the seeded default city is shown") {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithTag("current_weather_card")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("forecast_title").assertTextEquals("Taipei")
            composeRule.onNodeWithTag("current_temperature", useUnmergedTree = true)
                .assertTextEquals("28°")
        }

        step("Hourly forecast row is shown") {
            composeRule.onNodeWithTag("forecast_content")
                .performScrollToNode(hasTestTag("hourly_forecast_row"))
            composeRule.onNodeWithTag("hourly_forecast_row").assertExists()
        }

        step("Weekly forecast lists all seven days") {
            composeRule.onNodeWithTag("forecast_content")
                .performScrollToNode(hasTestTag("daily_row_2026-08-16"))
            composeRule.onNodeWithTag("daily_row_2026-08-16").assertExists()
        }
    }

    @Test
    fun addsCityViaSearchAndShowsItsForecast() = run {
        step("Open the city list") {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithTag("current_weather_card")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("cities_button").performClick()
            composeRule.onNodeWithTag("saved_cities_list").assertExists()
        }

        step("Search for a new city") {
            composeRule.onNodeWithTag("search_input").performTextInput("Kaohsiung")
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithTag("search_result_1673820")
                    .fetchSemanticsNodes().isNotEmpty()
            }
        }

        step("Add the city; app returns to its forecast") {
            composeRule.onNodeWithTag("add_city_1673820", useUnmergedTree = true).performClick()
            composeRule.waitUntilForecastTitle("Kaohsiung City")
        }
    }

    @Test
    fun switchesSelectedCityFromSavedList() = run {
        step("Open the city list") {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithTag("current_weather_card")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("cities_button").performClick()
        }

        step("Select Tokyo from the saved cities") {
            composeRule.onNodeWithTag("saved_cities_list")
                .performScrollToNode(hasTestTag("saved_city_1850147"))
            composeRule.onNodeWithTag("saved_city_1850147").performClick()
        }

        step("Forecast screen shows Tokyo") {
            composeRule.waitUntilForecastTitle("Tokyo")
        }
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.waitUntilForecastTitle(
        expected: String,
    ) {
        waitUntil(timeoutMillis = 10_000) {
            onAllNodes(hasTestTag("forecast_title") and hasText(expected))
                .fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("forecast_title").assertTextEquals(expected)
    }
}
