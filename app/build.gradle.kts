plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("dagger.hilt.android.plugin") version "2.48" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.6" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
}

android {
    namespace = "com.example.datingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.datingapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Firebase options
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"your-project-id\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"your-app-id\"")
        buildConfigField("String", "FIREBASE_MESSAGING_SENDER_ID", "\"your-sender-id\"")
        
        // Performance monitoring
        buildConfigField("Boolean", "PERF_ENABLED", "true")
        buildConfigField("Boolean", "CRASHLYTICS_ENABLED", "true")
        
        // Analytics
        buildConfigField("Boolean", "ANALYTICS_ENABLED", "true")
        buildConfigField("Float", "ANALYTICS_SAMPLE_RATE", "0.1f")
        
        // Debug options
        buildConfigField("Boolean", "DEBUG_LOGS_ENABLED", "true")
        buildConfigField("Boolean", "DEBUG_MODE", "false")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "DEBUG_MODE", "false")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("Boolean", "DEBUG_MODE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
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

    buildFeatures {
        compose = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
dependencies {
    // Core Android
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.permissions)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.functions.ktx)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.hilt.work)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.android.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.work.runtime)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.junit.jupiter)

    // Debug
    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.release)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Location
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.utils)

    // Audio/Video
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.mediasession)
    implementation(libs.recorder)

    // Analytics
    implementation(libs.mixpanel.android)
    implementation(libs.amplitude.android)

    // Utilities
    implementation(libs.timber)
    implementation(libs.lottie.compose)
    implementation(libs.stripe.android)

    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.github.dhaval2404:imagepicker:2.1' // for selecting images
    implementation 'de.hdodenhof:circleimageview:3.1.0' // for profile pictures
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    implementation 'com.google.firebase:firebase-messaging:23.4.1'
    implementation('org.jitsi.react:jitsi-meet-sdk:8.1.2') {
        transitive = true
    }
implementation('org.jitsi.react:jitsi-meet-sdk:8.1.2') {
    transitive = true
}
implementation 'com.google.android.gms:play-services-location:21.0.1'
implementation 'com.google.firebase:firebase-messaging:23.4.1'
