plugins {
    id("weatherforecast.android.library.compose")
}

android {
    namespace = "com.allenljf.weatherforecast.core.designsystem"
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.androidx.compose.material.icons.extended)
}
