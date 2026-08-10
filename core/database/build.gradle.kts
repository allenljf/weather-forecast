plugins {
    id("weatherforecast.android.library")
    id("weatherforecast.hilt")
}

android {
    namespace = "com.allenljf.weatherforecast.core.database"
}

// Exported schemas are what MigrationTestHelper replays to rebuild an old database version, and it
// loads them through the asset manager. Writing them straight into the unit-test assets source set
// keeps them off the published artifact and avoids an extra `sourceSets` wiring step.
ksp {
    arg("room.schemaLocation", "$projectDir/src/test/assets")
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
}
