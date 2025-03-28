plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mychatapptutorial"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nactik"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
}

dependencies {

    implementation(libs.appcompat)
    implementation ("com.hbb20:ccp:2.7.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.core:core-ktx:1.9.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.play.services.tasks)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("io.github.jan-tennert.supabase:postgrest-kt:1.4.7")
    implementation ("io.github.jan-tennert.supabase:realtime-kt:1.4.7")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("io.github.jan-tennert.supabase:storage-kt:1.4.7")
    implementation ("io.ktor:ktor-client-android:2.3.7")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}