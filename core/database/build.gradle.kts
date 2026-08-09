plugins {
    id("weatherforecast.android.library")
    id("weatherforecast.hilt")
}

android {
    namespace = "com.allenljf.weatherforecast.core.database"
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
}
