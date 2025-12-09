plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.skyzonebd.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.skyzonebd.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // SEO and App Indexing
        manifestPlaceholders["appName"] = "SkyzoneBD - B2B & B2C Shopping"
        manifestPlaceholders["appDescription"] = "Bangladesh's premier B2B and B2C marketplace"
        
        // API Base URL - Update this to your Vercel deployment URL
        buildConfigField("String", "BASE_URL", "\"https://skyzonebd.vercel.app/\"")
        buildConfigField("String", "API_URL", "\"https://skyzonebd.vercel.app/api/\"")
        
        // App metadata for Play Store
        resValue("string", "google_play_services_version", "12451000")
    }

    signingConfigs {
        create("release") {
            // Store passwords as environment variables for security
            // Set in your system: KEYSTORE_PASSWORD and KEY_PASSWORD
            storeFile = file("../skyzonebd-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "temp_password"
            keyAlias = "skyzonebd"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "temp_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://skyzonebd.vercel.app/\"")
            buildConfigField("String", "API_URL", "\"https://skyzonebd.vercel.app/api/\"")
        }
        debug {
            buildConfigField("String", "BASE_URL", "\"https://skyzonebd.vercel.app/\"")
            buildConfigField("String", "API_URL", "\"https://skyzonebd.vercel.app/api/\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Hilt for Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    
    // Kotlin Serialization (alternative to Gson)
    implementation(libs.kotlinx.serialization.json)
    
    // Room Database for local caching
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // Coil for Image Loading
    implementation(libs.coil.compose)
    
    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    
    // Paging 3 for pagination
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    
    // Accompanist for additional Compose utilities
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)
    
    // Timber for logging
    implementation(libs.timber)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
