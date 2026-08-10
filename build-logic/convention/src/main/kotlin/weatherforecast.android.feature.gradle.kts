import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("weatherforecast.android.library.compose")
    id("weatherforecast.hilt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(project(":core:domain"))
    "implementation"(project(":core:designsystem"))

    "implementation"(libs.findLibrary("androidx-appcompat").get())
    "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
    "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())

    "testImplementation"(project(":core:testing"))
    "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
}
