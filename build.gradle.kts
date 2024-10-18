// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:3.4.3")
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
 //   id ("org.jetbrains.kotlin.android") version ("1.7.20") apply false
    id ("com.google.dagger.hilt.android") version "2.52" apply false
   // id ("com.google.dagger.hilt.android") version ("2.44") apply false

}