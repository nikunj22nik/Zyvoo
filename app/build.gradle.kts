plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.yesitlab.zyvo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yesitlab.zyvo"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //sdp and ssp
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    // Dagger - Hilt
//    implementation ("com.google.dagger:hilt-android:2.44")
//    kapt ("com.google.dagger:hilt-android-compiler:2.44")
//    kapt ("androidx.hilt:hilt-compiler:1.0.0")
//    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.hbb20:ccp:2.6.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

    //OTP Dependency
    implementation ("com.github.aabhasr1:OtpView:v1.1.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //Image Picker
    implementation(libs.imagepicker)
    implementation(libs.glide)
    api("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.stfalcon-studio:StfalconPriceRangeBar-android:v1.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("com.google.android.libraries.places:places:2.4.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.material:material:1.11.0") // Check for the latest version
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.colourmoon:readmore-textview:v1.0.2")


}