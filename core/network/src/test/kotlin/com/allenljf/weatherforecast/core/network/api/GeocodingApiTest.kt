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

class GeocodingApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: GeocodingApi

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
            .create(GeocodingApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `searchCities parses results`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "results": [
                    {
                      "id": 1668341,
                      "name": "Taipei",
                      "latitude": 25.04,
                      "longitude": 121.53,
                      "country": "Taiwan",
                      "admin1": "Taipei"
                    },
                    {
                      "id": 1668399,
                      "name": "Taoyuan",
                      "latitude": 24.99,
                      "longitude": 121.31
                    }
                  ],
                  "generationtime_ms": 0.5
                }
                """.trimIndent(),
            ),
        )

        val response = api.searchCities(name = "Tai")

        val results = requireNotNull(response.results)
        assertEquals(2, results.size)
        with(results[0]) {
            assertEquals(1668341L, id)
            assertEquals("Taipei", name)
            assertEquals(25.04, latitude, 0.0)
            assertEquals(121.53, longitude, 0.0)
            assertEquals("Taiwan", country)
            assertEquals("Taipei", admin1)
        }
        // country / admin1 missing in payload -> default null, no crash.
        with(results[1]) {
            assertEquals("Taoyuan", name)
            assertNull(country)
            assertNull(admin1)
        }
    }

    @Test
    fun `searchCities returns null results when results field is absent`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"generationtime_ms":0.5}"""),
        )

        val response = api.searchCities(name = "zzzzzz-no-such-city")

        assertNull(response.results)
    }

    @Test
    fun `searchCities sends correct path and query parameters`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"generationtime_ms":0.5}"""),
        )

        api.searchCities(name = "Taipei")

        val request = server.takeRequest()
        val path = requireNotNull(request.path)
        assertTrue("path was $path", path.startsWith("/v1/search?"))
        val url = requireNotNull(request.requestUrl)
        assertEquals("Taipei", url.queryParameter("name"))
        assertEquals("10", url.queryParameter("count"))
        assertEquals("en", url.queryParameter("language"))
        assertEquals("json", url.queryParameter("format"))
    }

    @Test
    fun `searchCities throws HttpException with code on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        try {
            api.searchCities(name = "Taipei")
            fail("Expected HttpException to be thrown")
        } catch (exception: HttpException) {
            assertEquals(500, exception.code())
        }
    }
}
