plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.androidJunit5)
}

android {
    namespace = "com.jeong.cleanbookstore"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "com.jeong.cleanbookstore"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Android core
    implementation(libs.androidxCoreKtx)

    // Kotlin
    implementation(libs.bundles.kotlinBase)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hiltCompiler)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.roomCompiler)

    // Network
    implementation(libs.bundles.network)

    // Compose
    implementation(platform(libs.composeBomLib))
    implementation(libs.bundles.composeUi)

    // Unit test - JUnit4
    testImplementation(libs.bundles.unitTestJunit4)

    // Unit test - JUnit5
    testImplementation(platform(libs.junitBomLib))
    testImplementation(libs.bundles.unitTestJunit5)
    testRuntimeOnly(libs.junitJupiterEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)

    // Unit test helpers
    testImplementation(libs.bundles.unitTestHelpers)

    // Android test
    androidTestImplementation(libs.bundles.androidTest)

    // Hilt test
    testImplementation(libs.hiltTesting)
    kspTest(libs.hiltCompiler)

    androidTestImplementation(libs.hiltTesting)
    kspAndroidTest(libs.hiltCompiler)

    // Compose test
    androidTestImplementation(platform(libs.composeBomLib))
    androidTestImplementation(libs.bundles.composeTest)

    // Room test
    testImplementation(libs.roomTesting)

    // Debug only
    debugImplementation(libs.composeUiTooling)
    debugImplementation(libs.composeUiTestManifest)
}

hilt {
    enableExperimentalClasspathAggregation = true
    enableAggregatingTask = true
}
