plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "za.co.varsitycollege.st10204902.purrsonaltrainer"
    compileSdk = 34
    signingConfigs{
        create("debugFirebase"){
            storeFile = file("shared_debug.keystore")
            storePassword = "android"
            keyAlias = "shareddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "za.co.varsitycollege.st10204902.purrsonaltrainer"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    viewBinding {
        enable = true
    }
    buildTypes {
        debug{
            signingConfig = signingConfigs.getByName("debugFirebase")
        }
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("androidx.biometric:biometric:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation ("androidx.credentials:credentials:<latest version>")
    implementation ("androidx.credentials:credentials-play-services-auth:<latest version>")
    //implementation ("com.google.android.libraries.identity.googleid:googleid:<latest version>")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha04")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("com.google.firebase:firebase-messaging:23.1.1")


    // Use Firebase BOM for managing Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:31.5.0"))

    implementation(libs.anychart.android)

    //Test Implementations
    //JUnit5
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)

    //MockK for mocking
    testImplementation(libs.mockk)

    //Truth for assertions
    testImplementation(libs.truth)

    //RoboElectric for testing Components on JVM
    testImplementation(libs.robolectric)

    //Coroutines
    testImplementation (libs.kotlinx.coroutines.test)


    androidTestImplementation("androidx.test.ext:junit:1.1.5") // AndroidX JUnit extensions
    androidTestImplementation("androidx.test:runner:1.6.2") // AndroidX Test Runner

}
