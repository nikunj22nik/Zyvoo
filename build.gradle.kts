// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://sdk.withpersona.com/android/releases")
    }
    dependencies {
        // Updated Gradle plugin
        classpath("com.android.tools.build:gradle:8.4.2")

        // Hilt plugin
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
    }
}

plugins {
    // Use only one Kotlin plugin definition (remove duplicate)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // Hilt
    id("com.google.dagger.hilt.android") version "2.52" apply false

    // Google services (Firebase)
    alias(libs.plugins.google.gms.google.services) apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
}