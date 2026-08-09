package com.allenljf.weatherforecast.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class SelectedCityDataSourceTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private fun TestScope.createDataSource(): SelectedCityDataSource {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tmpFolder.root, "user_preferences_test.preferences_pb") },
        )
        return SelectedCityDataSource(dataStore)
    }

    @Test
    fun selectedCityId_isNullWhenNothingStored() = runTest {
        val dataSource = createDataSource()

        assertNull(dataSource.selectedCityId.first())
    }

    @Test
    fun setSelectedCityId_emitsStoredId() = runTest {
        val dataSource = createDataSource()

        dataSource.setSelectedCityId(1668341L)

        assertEquals(1668341L, dataSource.selectedCityId.first())
    }

    @Test
    fun clearSelectedCityId_emitsNullAgain() = runTest {
        val dataSource = createDataSource()

        dataSource.setSelectedCityId(1850147L)
        dataSource.clearSelectedCityId()

        assertNull(dataSource.selectedCityId.first())
    }

    @Test
    fun selectedCityId_emitsUpdatesInOrder() = runTest {
        val dataSource = createDataSource()

        dataSource.selectedCityId.test {
            assertNull(awaitItem())

            dataSource.setSelectedCityId(2643743L)
            assertEquals(2643743L, awaitItem())

            dataSource.clearSelectedCityId()
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
