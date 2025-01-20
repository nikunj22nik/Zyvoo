// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:3.4.3")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.52")
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
 //   id ("org.jetbrains.kotlin.android") version ("1.7.20") apply false
  //  id ("com.google.dagger.hilt.android") version ("2.52") apply false
    id ("com.google.dagger.hilt.android") version ("2.52") apply false
}