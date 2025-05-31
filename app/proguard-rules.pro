# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class your.package.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve generic type info for Gson TypeToken
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken
-keep class your.package.models.** { *; }  # Replace with your actual model package


# Keep type information for Gson's TypeToken
-keepattributes Signature
-keepattributes *Annotation*

# Keep the TypeToken class and its anonymous subclasses
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep all your model classes used in JSON parsing
-keep class com.business.zyvo.model.** { *; }  # Update to your actual model package

