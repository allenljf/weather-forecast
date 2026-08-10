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
                    path.startsWith("/v1/air-quality") -> jsonResponse(AIR_QUALITY_JSON)
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
                "weather_code": [2,2,3,3,61,61],
                "precipitation_probability": [10,15,20,35,70,80]
              },
              "daily": {
                "time": ["2026-08-10","2026-08-11","2026-08-12","2026-08-13","2026-08-14","2026-08-15","2026-08-16"],
                "weather_code": [2,61,95,3,0,80,1],
                "temperature_2m_max": [31.0,30.2,29.8,30.5,32.0,31.2,30.9],
                "temperature_2m_min": [26.1,25.8,25.5,25.9,26.4,26.0,25.7],
                "precipitation_probability_max": [30,80,90,40,10,60,20],
                "sunrise": ["2026-08-10T05:16","2026-08-11T05:17","2026-08-12T05:17","2026-08-13T05:18","2026-08-14T05:18","2026-08-15T05:19","2026-08-16T05:19"],
                "sunset": ["2026-08-10T18:35","2026-08-11T18:34","2026-08-12T18:33","2026-08-13T18:33","2026-08-14T18:32","2026-08-15T18:31","2026-08-16T18:30"]
              }
            }
        """.trimIndent()

        val AIR_QUALITY_JSON = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "current": {
                "time": "2026-08-10T12:00",
                "interval": 3600,
                "european_aqi": 42,
                "pm2_5": 12.4,
                "pm10": 20.1
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
