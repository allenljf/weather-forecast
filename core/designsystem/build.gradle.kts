plugins {
    id("weatherforecast.android.library.compose")
}

android {
    namespace = "com.allenljf.weatherforecast.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
}
