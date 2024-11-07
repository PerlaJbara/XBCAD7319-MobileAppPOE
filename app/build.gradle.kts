plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.opsc7311poe.xbcad_antoniemotors"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.opsc7311poe.xbcad_antoniemotors"
        minSdk = 27
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    // Splash API
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.airbnb.android:lottie:6.0.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.biometric:biometric:1.4.0-alpha02")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.biometric.ktx)
    implementation (libs.androidx.biometric)
    implementation (libs.firebase.auth.ktx.v2101)
    implementation(libs.firebase.storage.ktx)

    //Testing
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.androidx.recyclerview)
    //implementation(libs.firebase.functions)

    implementation ("com.google.firebase:firebase-bom:32.2.0")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
    implementation ("com.google.android.gms:play-services-auth:20.1.0") // or the latest version
    implementation ("com.google.android.gms:play-services-tasks:17.2.1") // or the latest version
    //implementation ("com.google.firebase:firebase-functions:21.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    //annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    //for graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
