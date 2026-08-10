package com.allenljf.weatherforecast.e2e

import androidx.test.core.app.takeScreenshot
import androidx.test.core.graphics.writeToTestStorage
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Captures a device screenshot when each test finishes (pass or fail) and
 * writes it to androidx test storage. AGP pulls the files back to
 * `app/build/outputs/connected_android_test_additional_output/` after the run.
 */
class ScreenshotOnTestFinishedRule : TestWatcher() {

    override fun succeeded(description: Description) {
        capture(description, "PASSED")
    }

    override fun failed(e: Throwable?, description: Description) {
        capture(description, "FAILED")
    }

    private fun capture(description: Description, status: String) {
        runCatching {
            takeScreenshot().writeToTestStorage("${description.methodName}_$status")
        }
    }
}
