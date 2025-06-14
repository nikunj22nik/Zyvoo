plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.google.gms.google.services)
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
    id ("kotlin-parcelize")
}

android{

    namespace = "com.business.zyvo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.business.zyvo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val BASE_URL = project.property("BASE_URL")
            buildConfigField("String", "BASE_URL", "${BASE_URL}")

            val MEDIA_URL = project.property("MEDIA_URL")
            buildConfigField("String", "MEDIA_URL", "${MEDIA_URL}")

            val STRIPE_KEY = project.property("STRIPE_KEY")
            buildConfigField("String", "STRIPE_KEY", "${STRIPE_KEY}")

            val templateID = project.property("templateID")
            buildConfigField("String", "templateID", "${templateID}")


            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            val BASE_URL = project.property("BASE_URL")
            buildConfigField("String", "BASE_URL", "${BASE_URL}")

            val MEDIA_URL = project.property("MEDIA_URL")
            buildConfigField("String", "MEDIA_URL", "${MEDIA_URL}")

            val STRIPE_KEY = project.property("STRIPE_KEY")
            buildConfigField("String", "STRIPE_KEY", "${STRIPE_KEY}")
            
            val templateID = project.property("templateID")
            buildConfigField("String", "templateID", "${templateID}")
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
        buildConfig = true
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
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.annotations)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
 //   implementation("com.github.bumptech.glide:glide:4.8.0")  //By Shrawan
  //  annotationProcessor("com.github.bumptech.glide:compiler:4.8.0")  //By Shrawan
    //sdp and ssp
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    // Dagger - Hilt
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


    implementation ("com.airbnb.android:lottie:3.4.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
   // implementation ("com.github.ozcanalasalvar:otpview:2.0.1")
    //Image Picker
    implementation(libs.imagepicker)
    implementation(libs.glide)
   // api("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.stfalcon-studio:StfalconPriceRangeBar-android:v1.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("com.google.android.libraries.places:places:2.4.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
   // implementation("com.google.android.material:material:1.11.0") // Check for the latest version
    //implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.colourmoon:readmore-textview:v1.0.2")
    //Spinner
    implementation("com.github.skydoves:powerspinner:1.2.7")
// Paging library
   // implementation("androidx.paging:paging-runtime:3.1.0")

    //persona API
    implementation (libs.sdk2.inquiry)
    //firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation("com.twilio:conversations-android:1.4.1")
    implementation("com.getkeepsafe.relinker:relinker:1.4.4") // Explicitly include Relinker
   // implementation ("com.github.barteksc:pdfium-android:1.9.0")
    //Pdf lib.
   // implementation ("com.github.barteksc:pdfium-android:1.9.0")


    implementation("com.github.jaiselrahman:FilePicker:1.3.2")

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    //Stripe SDK
    implementation("com.stripe:stripe-android:16.3.0")

    implementation ("com.appsflyer:af-android-sdk:6.12.1")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.19")

    implementation ("kr.co.prnd:readmore-textview:1.0.0")



}