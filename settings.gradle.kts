pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Official Google libraries (e.g., AndroidX, Play Services)
        mavenCentral() // Common Java and Kotlin libraries (e.g., Retrofit, OkHttp)
        maven("https://jitpack.io") // Community packages (e.g., GitHub-hosted libraries)
        maven("https://sdk.withpersona.com/android/releases") // Persona SDK
    }
}


rootProject.name = "ZYVO"
include(":app")
 