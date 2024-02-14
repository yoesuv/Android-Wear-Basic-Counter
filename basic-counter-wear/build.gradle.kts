plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {

    val appKeyAlias: String by project
    val appKeyPassword: String by project
    val appStorePassword: String by project

    signingConfigs {
        create("config") {
            storeFile = file("../my-wear.keystore")
            keyAlias = appKeyAlias
            keyPassword = appKeyPassword
            storePassword = appStorePassword
        }
    }

    namespace = "com.yoesuv.basiccounter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yoesuv.basiccounter"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(WearableDependencies.wearable)
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.wear.compose:compose-material:1.1.2")
    implementation("androidx.wear.compose:compose-foundation:1.1.2")
    implementation(AndroidDependencies.compose)
    implementation("androidx.core:core-splashscreen:1.0.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation(project(":source"))

    implementation(AndroidDependencies.iconExtended)
    implementation(AndroidDependencies.lifeData)
    implementation(CoroutineDependencies.playService)
}