plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.benjamin.notifvolwidget"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.benjamin.notifvolwidget"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// No dependencies: the widget only uses framework APIs (AudioManager,
// RemoteViews, AppWidgetManager) directly, so there's nothing here to add -
// kept intentionally dependency-free.
