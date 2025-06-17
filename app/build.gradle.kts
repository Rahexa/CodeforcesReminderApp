plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.codeforcestracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.codeforcestracker"
        minSdk = 30 // Adjust if your phone's Android version is lower
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // Match your Kotlin version
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1") // For WorkManager
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // For API calls
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // For JSON parsing
}