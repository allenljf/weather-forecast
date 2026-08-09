package com.allenljf.weatherforecast.e2e

import com.allenljf.weatherforecast.di.TestBaseUrlModule
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.rules.ExternalResource

/**
 * Starts a MockWebServer serving canned Open-Meteo responses. Storage
 * isolation comes from TestDatabaseModule/TestDataStoreModule, which give
 * each test component a fresh in-memory DB and a unique DataStore file.
 */
class MockWeatherServerRule : ExternalResource() {

    lateinit var server: MockWebServer
        private set

    override fun before() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                return when {
                    path.startsWith("/v1/forecast") -> jsonResponse(FORECAST_JSON)
                    path.startsWith("/v1/search") -> jsonResponse(GEOCODING_JSON)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start(TestBaseUrlModule.MOCK_SERVER_PORT)
    }

    override fun after() {
        server.shutdown()
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    companion object {
        val FORECAST_JSON = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "timezone": "Asia/Taipei",
              "current": {
                "time": "2026-08-10T12:00",
                "temperature_2m": 28.4,
                "apparent_temperature": 31.2,
                "relative_humidity_2m": 68,
                "weather_code": 2,
                "wind_speed_10m": 11.5
              },
              "hourly": {
                "time": ["2026-08-10T12:00","2026-08-10T13:00","2026-08-10T14:00","2026-08-10T15:00","2026-08-10T16:00","2026-08-10T17:00"],
                "temperature_2m": [28.4,29.0,29.5,29.1,28.6,28.0],
                "weather_code": [2,2,3,3,61,61]
              },
              "daily": {
                "time": ["2026-08-10","2026-08-11","2026-08-12","2026-08-13","2026-08-14","2026-08-15","2026-08-16"],
                "weather_code": [2,61,95,3,0,80,1],
                "temperature_2m_max": [31.0,30.2,29.8,30.5,32.0,31.2,30.9],
                "temperature_2m_min": [26.1,25.8,25.5,25.9,26.4,26.0,25.7]
              }
            }
        """.trimIndent()

        val GEOCODING_JSON = """
            {
              "results": [
                {
                  "id": 1673820,
                  "name": "Kaohsiung City",
                  "latitude": 22.61626,
                  "longitude": 120.31333,
                  "country": "Taiwan",
                  "admin1": "Kaohsiung"
                }
              ]
            }
        """.trimIndent()
    }
}
