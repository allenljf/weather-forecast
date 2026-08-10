package com.allenljf.weatherforecast.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Verifies [WeatherDatabase.MIGRATION_1_2] adds the forecast cache table without dropping the
 * cities a user had already saved on version 1.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WeatherDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsSavedCitiesAndAddsForecastCacheTable() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO saved_cities (id, name, country, latitude, longitude, position) " +
                    "VALUES (1668341, 'Taipei', 'Taiwan', 25.0478, 121.5319, 0)",
            )
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            WeatherDatabase.MIGRATION_1_2,
        )

        db.query("SELECT id, name, position FROM saved_cities").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(1668341L, cursor.getLong(0))
            assertEquals("Taipei", cursor.getString(1))
            assertEquals(0, cursor.getInt(2))
        }

        db.execSQL(
            "INSERT INTO cached_forecasts (cityId, payload, fetchedAtMillis) " +
                "VALUES (1668341, '{\"daily\":[]}', 1700000000000)",
        )
        db.query("SELECT cityId, payload, fetchedAtMillis FROM cached_forecasts").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(1668341L, cursor.getLong(0))
            assertEquals("""{"daily":[]}""", cursor.getString(1))
            assertEquals(1_700_000_000_000L, cursor.getLong(2))
        }
        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
