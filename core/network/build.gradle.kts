plugins {
    id("weatherforecast.android.library")
    id("weatherforecast.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.allenljf.weatherforecast.core.network"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.okhttp.mockwebserver)
}
