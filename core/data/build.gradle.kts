plugins {
    id("weatherforecast.android.library")
    id("weatherforecast.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.allenljf.weatherforecast.core.data"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
}
