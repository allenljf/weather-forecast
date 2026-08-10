plugins {
    id("weatherforecast.jvm.library")
}

dependencies {
    api(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
