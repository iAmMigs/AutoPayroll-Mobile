plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    // The kotlin-compose plugin is necessary for Jetpack Compose
    alias(libs.plugins.kotlin.compose)
    id("androidx.navigation.safeargs.kotlin")


}

android {
    namespace = "com.example.autopayroll_mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.autopayroll_mobile"
        minSdk = 26
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
                "proguard-rules.pro"
            )
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
        viewBinding = true
        // You must enable compose for it to work
        compose = true
    }

    // You need to add this block for the Compose compiler plugin
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Use a version compatible with your Kotlin and Compose versions
    }
}

dependencies {
    // Original dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.fragment:fragment-ktx:1.7.1")

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // CameraX
    val camerax_version = "1.3.4"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Squareup (Retrofit/OkHttp)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson
    implementation("com.google.code.gson:gson:2.9.0")

    // Compose Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // --- Corrected Jetpack Compose & Lifecycle Dependencies ---

    // Define versions
    val lifecycle_version = "2.8.0"
    val compose_bom_version = "2024.05.00"
    val activity_compose_version = "1.9.0"

    // Lifecycle (ViewModel, LiveData, etc.)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version") // For collecting flows as state
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")

    // Import the Compose Bill of Materials (BoM)
    implementation(platform("androidx.compose:compose-bom:$compose_bom_version"))

    // Compose Dependencies (versions managed by the BoM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata") // << THIS IS THE FIX

    // Integration with Activities
    implementation("androidx.activity:activity-compose:$activity_compose_version")
    implementation("io.coil-kt:coil-compose:2.6.0")

    //Icons
    implementation("androidx.compose.material:material-icons-extended")

    //Nav
    implementation(libs.navigation.compose)
    implementation(libs.navigation.fragment.ktx)

    // Tooling for debugging and inspection
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}