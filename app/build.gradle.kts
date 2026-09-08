plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.javadoh.plantasmedicinales"
    compileSdk = 35

    buildFeatures {
        aidl = true
    }

    defaultConfig {
        applicationId = "com.javadoh.plantasmedicinalesnaturales"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
// Required for the modernized Kotlin code
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.facebook.android:facebook-login:16.3.0")
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // Required for DynamicBackImage.kt
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
}