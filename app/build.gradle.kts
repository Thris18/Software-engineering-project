plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    lint {
        disable += "AutoboxingStateCreation"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }

}

dependencies {

    // Core Android dependencies
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx.v270)
    implementation(libs.androidx.activity.compose.v182)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom.v20230800))
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.animation.graphics)



    // Lifecycle og LocalLifecycleOwner
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v270)
    implementation(libs.androidx.lifecycle.viewmodel.compose.v290)
    implementation(libs.androidx.lifecycle.runtime.compose.v290)
    implementation(libs.androidx.lifecycle.common.java8)

    // Nettverk
    implementation(libs.okhttp.v4110)
    implementation(libs.logging.interceptor.v4110)

    // NetCDF/GRIB parser dependencies
    implementation(libs.cdm.core)
    implementation(libs.grib)
    implementation(libs.guava)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat.resources) // Required by NetCDF
    implementation(libs.play.services.maps)
    implementation(libs.androidx.animation.graphics.android)
    implementation(libs.androidx.exifinterface) // Required by NetCDF

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // MapLibre
    implementation(libs.android.sdk)
    implementation(libs.android.plugin.annotation.v9.v201)

    // JSON
    implementation(libs.json)

    implementation(libs.coil.compose)

    implementation(libs.androidx.material.icons.extended)

    // Til GIF
    implementation(libs.coil.gif)



    // Bakoverkompitabel løsning for ZonedTimeDate
    implementation (libs.threetenabp)

    // Lottie animasjoner
    implementation (libs.lottie)
    implementation (libs.lottie.compose)
    implementation(libs.play.services.location)



}