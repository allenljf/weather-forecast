plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("weatherforecast.hilt")
}

android {
    namespace = "com.allenljf.weatherforecast"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.allenljf.weatherforecast"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.allenljf.weatherforecast.core.testing.HiltTestRunner"
        // Lets instrumented tests write screenshots/files to test storage;
        // AGP pulls them back to build/outputs after the run.
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Kaspresso 1.6.x publishes strictly-locked transitive versions that clash
// with the newer Espresso/androidx stack; force the newer versions for tests.
configurations.matching { it.name.contains("AndroidTest") }.configureEach {
    resolutionStrategy {
        force("androidx.concurrent:concurrent-futures:1.2.0")
    }
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
    }
}

// AGP's connected test tasks don't support Gradle's testLogging, so parse the
// XML reports and echo per-test results to the console after the run.
val printConnectedTestResults = tasks.register("printConnectedTestResults") {
    description = "Prints per-test results from connected androidTest XML reports."
    val resultsDir = layout.buildDirectory.dir("outputs/androidTest-results/connected")
    doLast {
        val dir = resultsDir.get().asFile
        if (!dir.exists()) return@doLast
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        dir.walkTopDown()
            .filter { it.isFile && it.name.startsWith("TEST-") && it.extension == "xml" }
            .forEach { file ->
                val doc = factory.newDocumentBuilder().parse(file)
                val cases = doc.getElementsByTagName("testcase")
                for (i in 0 until cases.length) {
                    val case = cases.item(i) as org.w3c.dom.Element
                    val className = case.getAttribute("classname").substringAfterLast('.')
                    val status = when {
                        case.getElementsByTagName("failure").length > 0 ||
                            case.getElementsByTagName("error").length > 0 -> "FAILED"

                        case.getElementsByTagName("skipped").length > 0 -> "SKIPPED"
                        else -> "PASSED"
                    }
                    println("$className > ${case.getAttribute("name")} $status")
                }
            }
    }
}

tasks.matching { it.name.startsWith("connected") && it.name.endsWith("AndroidTest") }.configureEach {
    finalizedBy(printConnectedTestResults)
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:forecast"))
    implementation(project(":feature:cities"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:network"))
    androidTestImplementation(project(":core:database"))
    androidTestImplementation(project(":core:datastore"))
    androidTestImplementation(libs.room.runtime)
    androidTestImplementation(libs.androidx.datastore.preferences)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestUtil(libs.androidx.test.services)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.okhttp.mockwebserver)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
