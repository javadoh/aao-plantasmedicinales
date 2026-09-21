plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.javadoh.plantasmedicinales"
    compileSdk = 37

    aaptOptions {
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!Thumbs.db:!picasa.ini:!*~"
    }

    buildFeatures {
        aidl = true
    }

    defaultConfig {
        applicationId = "com.javadoh.plantasmedicinalesnaturales"
        minSdk = 26
        targetSdk = 37
        versionCode = 23
        versionName = "2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
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
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.facebook.android:facebook-login:16.3.0")
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // Required for DynamicBackImage.kt
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.30")
}