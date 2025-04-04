pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // For MapLibre
        maven { url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-all") } // For NetCDF-Java
        // Mapbox SDK repositories
        maven { url = uri("https://api.mapbox.com/downloads/v2/releases/maven") }
        maven { url = uri("https://mapbox.bintray.com/mapbox") }
    }
}

rootProject.name = "Team45FiskeriApp"
include(":app")
