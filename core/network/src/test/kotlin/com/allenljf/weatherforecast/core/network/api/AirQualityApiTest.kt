package com.allenljf.weatherforecast.core.network.api

import com.allenljf.weatherforecast.core.network.di.NetworkModule
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AirQualityApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AirQualityApi

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
            .create(AirQualityApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getAirQuality parses full sample response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_AIR_QUALITY_JSON))

        val response = api.getAirQuality(latitude = 25.0, longitude = 121.5)

        assertNotNull(response.current)
        val current = requireNotNull(response.current)
        assertEquals("2026-08-10T10:00", current.time)
        assertEquals(50, current.europeanAqi)
        assertEquals(31.7, requireNotNull(current.pm25), 0.0)
        assertEquals(34.7, requireNotNull(current.pm10), 0.0)
    }

    @Test
    fun `getAirQuality returns null current when field is absent`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(SAMPLE_AIR_QUALITY_JSON_WITHOUT_CURRENT),
        )

        val response = api.getAirQuality(latitude = 25.0, longitude = 121.5)

        assertNull(response.current)
    }

    @Test
    fun `getAirQuality parses null measurement values`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(SAMPLE_AIR_QUALITY_JSON_WITH_NULL_VALUES),
        )

        val response = api.getAirQuality(latitude = 25.0, longitude = 121.5)

        val current = requireNotNull(response.current)
        assertEquals("2026-08-10T10:00", current.time)
        assertNull(current.europeanAqi)
        assertNull(current.pm25)
        assertNull(current.pm10)
    }

    @Test
    fun `getAirQuality ignores unknown fields in response`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(SAMPLE_AIR_QUALITY_JSON_WITH_UNKNOWN_FIELDS),
        )

        val response = api.getAirQuality(latitude = 25.0, longitude = 121.5)

        assertEquals(50, requireNotNull(response.current).europeanAqi)
    }

    @Test
    fun `getAirQuality sends correct path and query parameters`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_AIR_QUALITY_JSON))

        api.getAirQuality(latitude = 25.0, longitude = 121.5)

        val request = server.takeRequest()
        val path = requireNotNull(request.path)
        assertTrue("path was $path", path.startsWith("/v1/air-quality?"))
        val url = requireNotNull(request.requestUrl)
        assertEquals("25.0", url.queryParameter("latitude"))
        assertEquals("121.5", url.queryParameter("longitude"))
        assertEquals("european_aqi,pm2_5,pm10", url.queryParameter("current"))
        assertEquals("auto", url.queryParameter("timezone"))
    }

    @Test
    fun `getAirQuality throws HttpException with code on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":true}"""))

        try {
            api.getAirQuality(latitude = 25.0, longitude = 121.5)
            fail("Expected HttpException to be thrown")
        } catch (exception: HttpException) {
            assertEquals(500, exception.code())
        }
    }

    companion object {
        private val SAMPLE_AIR_QUALITY_JSON = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "current": {
                "time": "2026-08-10T10:00",
                "interval": 3600,
                "european_aqi": 50,
                "pm2_5": 31.7,
                "pm10": 34.7
              }
            }
        """.trimIndent()

        private val SAMPLE_AIR_QUALITY_JSON_WITHOUT_CURRENT = """
            {
              "latitude": 25.0,
              "longitude": 121.5
            }
        """.trimIndent()

        private val SAMPLE_AIR_QUALITY_JSON_WITH_NULL_VALUES = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "current": {
                "time": "2026-08-10T10:00",
                "european_aqi": null,
                "pm2_5": null,
                "pm10": null
              }
            }
        """.trimIndent()

        private val SAMPLE_AIR_QUALITY_JSON_WITH_UNKNOWN_FIELDS = """
            {
              "latitude": 25.0,
              "longitude": 121.5,
              "generationtime_ms": 0.123,
              "utc_offset_seconds": 28800,
              "timezone": "Asia/Taipei",
              "timezone_abbreviation": "GMT+8",
              "elevation": 10.0,
              "current_units": {"pm2_5": "μg/m³"},
              "current": {
                "time": "2026-08-10T10:00",
                "interval": 3600,
                "european_aqi": 50,
                "pm2_5": 31.7,
                "pm10": 34.7
              }
            }
        """.trimIndent()
    }
}
