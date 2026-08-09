plugins {
    id("weatherforecast.android.library")
    id("weatherforecast.hilt")
}

android {
    namespace = "com.allenljf.weatherforecast.core.testing"
}

dependencies {
    api(project(":core:domain"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)

    implementation(libs.androidx.test.runner)
    implementation(libs.hilt.android.testing)
}
