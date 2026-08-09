import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("weatherforecast.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    val bom = libs.findLibrary("androidx-compose-bom").get()
    "implementation"(platform(bom))
    "implementation"(libs.findLibrary("androidx-compose-ui").get())
    "implementation"(libs.findLibrary("androidx-compose-ui-graphics").get())
    "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
    "implementation"(libs.findLibrary("androidx-compose-material3").get())
    "implementation"(libs.findLibrary("androidx-compose-material-icons-extended").get())
    "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
    "debugImplementation"(libs.findLibrary("androidx-compose-ui-test-manifest").get())
    "androidTestImplementation"(platform(bom))
    "androidTestImplementation"(libs.findLibrary("androidx-compose-ui-test-junit4").get())
}
