package com.allenljf.weatherforecast.core.network.api

import com.allenljf.weatherforecast.core.network.di.NetworkModule
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ForecastApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ForecastApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                NetworkModule.provideJson().asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(ForecastApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getForecast parses full sample response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_FORECAST_JSON))

        val response = api.getForecast(latitude = 25.0, longitude = 121.5)

        assertEquals(25.0, response.latitude, 0.0)
        assertEquals(121.5, response.longitude, 0.0)
        assertEquals("Asia/Taipei", response.timezone)

        with(response.current) {
            assertEquals("2026-08-10T12:00", time)
            assertEquals(30.5, temperature2m, 0.0)
            assertEquals(34.2, apparentTemperature, 0.0)
            assertEquals(70, relativeHumidity2m)
            assertEquals(2, weatherCode)
            assertEquals(12.3, windSpeed10m, 0.0)
        }

        with(response.hourly) {
            assertEquals(listOf("2026-08-10T00:00", "2026-08-10T01:00"), time)
            assertEquals(listOf(27.1, 26.8), temperature2m)
            assertEquals(listOf(1, 3), weatherCode)
            assertEquals(listOf(15, 20), precipitationProbability)
        }

        with(response.daily) {
            assertEquals(listOf("2026-08-10", "2026-08-11"), time)
            assertEquals(listOf(61, 2), weatherCode)
            assertEquals(listOf(31.0, 32.5), temperature2mMax)
            assertEquals(listOf(26.0, 25.5), temperature2mMin)
            assertEquals(listOf("2026-08-10T05:16", "2026-08-11T05:17"), sunrise)
            assertEquals(listOf("2026-08-10T18:35", "2026-08-11T18:34"), sunset)
            assertEquals(listOf(80, 10), precipitationProbabilityMax)
        }
    }

    @Test
    fun `getForecast parses response without precipitation and sun fields`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(SAMPLE_FORECAST_JSON_WITHOUT_NEW_FIELDS),
        )

        val response = api.getForecast(latitude = 25.0, longitude = 121.5)

        assertEquals(listOf(27.1, 26.8), response.hourly.temperature2m)
        assertNull(response.hourly.precipitationProbability)
        assertNull(response.daily.sunrise)
        assertNull(response.daily.sunset)
        assertNull(response.daily.precipitationProbabilityMax)
    }

    @Test
    fun `getForecast parses null entries inside precipitation probability arrays`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(SAMPLE_FORECAST_JSON_WITH_NULL_ENTRIES),
        )

        val response = api.getForecast(latitude = 25.0, longitude = 121.5)

        assertEquals(listOf(15, null), response.hourly.precipitationProbability)
        assertEquals(listOf(null), response.daily.precipitationProbabilityMax)
    }

    @Test
    fun `getForecast ignores unknown fields in response`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(SAMPLE_FORECAST_JSON_WITH_UNKNOWN_FIELDS),
        )

        val response = api.getForecast(latitude = 25.0, longitude = 121.5)

        assertEquals("Asia/Taipei", response.timezone)
        assertEquals(30.5, response.current.temperature2m, 0.0)
    }

    @Test
    fun `getForecast sends correct path and query parameters`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_FORECAST_JSON))

        api.getForecast(latitude = 25.0, longitude = 121.5)

        val request = server.takeRequest()
        val path = requireNotNull(request.path)
        assertTrue("path was $path", path.startsWith("/v1/forecast?"))
        val url = requireNotNull(request.requestUrl)
        assertEquals("25.0", url.queryParameter("latitude"))
        assertEquals("121.5", url.queryParameter("longitude"))
        assertEquals(
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m",
            url.queryParameter("current"),
        )
        assertEquals(
            "temperature_2m,weather_code,precipitation_probability",
            url.queryParameter("hourly"),
        )
        assertEquals(
            "weather_code,temperature_2m_max,temperature_2m_min," +
                "sunrise,sunset,precipitation_probability_max",
            url.queryParameter("daily"),
        )
        val daily = requireNotNull(url.queryParameter("daily"))
        assertTrue("daily was $daily", daily.contains("sunrise"))
        assertTrue("daily was $daily", daily.contains("sunset"))
        assertTrue("daily was $daily", daily.contains("precipitation_probability_max"))
        val hourly = requireNotNull(url.queryParameter("hourly"))
        assertTrue("hourly was $hourly", hourly.contains("precipitation_probability"))
        assertEquals("auto", url.queryParameter("timezone"))
        assertEquals("7", url.queryParameter("forecast_days"))
    }

    @Test
    fun `getForecast throws HttpException with code on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":true}"""))

        try {
            api.getForecast(latitude = 25.0, longitude = 121.5)
            fail("Expected HttpException to be thrown")
        } catch (exception: HttpException) {
            assertEquals(500, exception.code())
        }
    }

    @Test
    fun `getForecast throws HttpException on client error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":true,"reason":"Latitude must be in range"}"""),
        )

        try {
            api.getForecast(latitude = 999.0, longitude = 121.5)
            fail("Expected HttpException to be thrown")
        } catch (exception: HttpException) {
            assertEquals(400, exception.code())
        }
    }

    companion object {
        private val SAMPLE_FORECAST_JSON = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "timezone": "Asia/Taipei",
              "current": {
                "time": "2026-08-10T12:00",
                "temperature_2m": 30.5,
                "apparent_temperature": 34.2,
                "relative_humidity_2m": 70,
                "weather_code": 2,
                "wind_speed_10m": 12.3
              },
              "hourly": {
                "time": ["2026-08-10T00:00", "2026-08-10T01:00"],
                "temperature_2m": [27.1, 26.8],
                "weather_code": [1, 3],
                "precipitation_probability": [15, 20]
              },
              "daily": {
                "time": ["2026-08-10", "2026-08-11"],
                "weather_code": [61, 2],
                "temperature_2m_max": [31.0, 32.5],
                "temperature_2m_min": [26.0, 25.5],
                "sunrise": ["2026-08-10T05:16", "2026-08-11T05:17"],
                "sunset": ["2026-08-10T18:35", "2026-08-11T18:34"],
                "precipitation_probability_max": [80, 10]
              }
            }
        """.trimIndent()

        private val SAMPLE_FORECAST_JSON_WITHOUT_NEW_FIELDS = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "timezone": "Asia/Taipei",
              "current": {
                "time": "2026-08-10T12:00",
                "temperature_2m": 30.5,
                "apparent_temperature": 34.2,
                "relative_humidity_2m": 70,
                "weather_code": 2,
                "wind_speed_10m": 12.3
              },
              "hourly": {
                "time": ["2026-08-10T00:00", "2026-08-10T01:00"],
                "temperature_2m": [27.1, 26.8],
                "weather_code": [1, 3]
              },
              "daily": {
                "time": ["2026-08-10", "2026-08-11"],
                "weather_code": [61, 2],
                "temperature_2m_max": [31.0, 32.5],
                "temperature_2m_min": [26.0, 25.5]
              }
            }
        """.trimIndent()

        private val SAMPLE_FORECAST_JSON_WITH_NULL_ENTRIES = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "timezone": "Asia/Taipei",
              "current": {
                "time": "2026-08-10T12:00",
                "temperature_2m": 30.5,
                "apparent_temperature": 34.2,
                "relative_humidity_2m": 70,
                "weather_code": 2,
                "wind_speed_10m": 12.3
              },
              "hourly": {
                "time": ["2026-08-10T00:00", "2026-08-10T01:00"],
                "temperature_2m": [27.1, 26.8],
                "weather_code": [1, 3],
                "precipitation_probability": [15, null]
              },
              "daily": {
                "time": ["2026-08-10"],
                "weather_code": [61],
                "temperature_2m_max": [31.0],
                "temperature_2m_min": [26.0],
                "sunrise": ["2026-08-10T05:16"],
                "sunset": ["2026-08-10T18:35"],
                "precipitation_probability_max": [null]
              }
            }
        """.trimIndent()

        private val SAMPLE_FORECAST_JSON_WITH_UNKNOWN_FIELDS = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "generationtime_ms": 0.123,
              "utc_offset_seconds": 28800,
              "timezone": "Asia/Taipei",
              "timezone_abbreviation": "GMT+8",
              "elevation": 10.0,
              "current_units": {"temperature_2m": "°C"},
              "current": {
                "time": "2026-08-10T12:00",
                "interval": 900,
                "temperature_2m": 30.5,
                "apparent_temperature": 34.2,
                "relative_humidity_2m": 70,
                "weather_code": 2,
                "wind_speed_10m": 12.3
              },
              "hourly": {
                "time": ["2026-08-10T00:00"],
                "temperature_2m": [27.1],
                "weather_code": [1]
              },
              "daily": {
                "time": ["2026-08-10"],
                "weather_code": [61],
                "temperature_2m_max": [31.0],
                "temperature_2m_min": [26.0]
              }
            }
        """.trimIndent()
    }
}
