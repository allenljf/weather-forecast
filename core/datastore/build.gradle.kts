plugins {
    id("weatherforecast.android.library")
    id("weatherforecast.hilt")
}

android {
    namespace = "com.allenljf.weatherforecast.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
}
