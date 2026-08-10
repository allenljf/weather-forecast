package com.allenljf.weatherforecast.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allenljf.weatherforecast.core.database.WeatherDatabase
import com.allenljf.weatherforecast.core.database.entity.CachedForecastEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ForecastCacheDaoTest {

    private lateinit var db: WeatherDatabase
    private lateinit var dao: ForecastCacheDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.forecastCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getByCityId_returnsNullWhenTableIsEmpty() = runTest {
        assertNull(dao.getByCityId(1L))
    }

    @Test
    fun upsert_thenGetByCityId_returnsStoredRow() = runTest {
        dao.upsert(
            CachedForecastEntity(
                cityId = 1668341L,
                payload = """{"daily":[]}""",
                fetchedAtMillis = 1_700_000_000_000L,
            ),
        )

        val cached = dao.getByCityId(1668341L)

        assertNotNull(cached)
        assertEquals(1668341L, cached!!.cityId)
        assertEquals("""{"daily":[]}""", cached.payload)
        assertEquals(1_700_000_000_000L, cached.fetchedAtMillis)
    }

    @Test
    fun getByCityId_returnsNullForUncachedCity() = runTest {
        dao.upsert(entity(cityId = 1L))

        assertNull(dao.getByCityId(2L))
    }

    @Test
    fun upsert_withSameCityId_replacesExistingRow() = runTest {
        dao.upsert(entity(cityId = 1L, payload = "old", fetchedAtMillis = 100L))
        dao.upsert(entity(cityId = 1L, payload = "new", fetchedAtMillis = 200L))

        val cached = dao.getByCityId(1L)

        assertEquals("new", cached?.payload)
        assertEquals(200L, cached?.fetchedAtMillis)
        assertEquals(1, countRows())
    }

    @Test
    fun deleteByCityId_removesOnlyThatCity() = runTest {
        dao.upsert(entity(cityId = 1L, payload = "taipei"))
        dao.upsert(entity(cityId = 2L, payload = "tokyo"))

        dao.deleteByCityId(1L)

        assertNull(dao.getByCityId(1L))
        assertEquals("tokyo", dao.getByCityId(2L)?.payload)
    }

    @Test
    fun deleteByCityId_isNoOpForUnknownCity() = runTest {
        dao.upsert(entity(cityId = 1L, payload = "taipei"))

        dao.deleteByCityId(99L)

        assertEquals("taipei", dao.getByCityId(1L)?.payload)
    }

    @Test
    fun clear_removesEveryRow() = runTest {
        dao.upsert(entity(cityId = 1L))
        dao.upsert(entity(cityId = 2L))

        dao.clear()

        assertNull(dao.getByCityId(1L))
        assertNull(dao.getByCityId(2L))
        assertEquals(0, countRows())
    }

    /** Row count is asserted directly so the DAO surface stays limited to what production needs. */
    private fun countRows(): Int =
        db.query("SELECT COUNT(*) FROM cached_forecasts", emptyArray()).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun entity(
        cityId: Long,
        payload: String = """{"daily":[]}""",
        fetchedAtMillis: Long = 1_700_000_000_000L,
    ) = CachedForecastEntity(
        cityId = cityId,
        payload = payload,
        fetchedAtMillis = fetchedAtMillis,
    )
}
