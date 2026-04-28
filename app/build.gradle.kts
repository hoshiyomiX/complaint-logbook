plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.hoshiyomix.complaintlogbook"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hoshiyomix.complaintlogbook"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.5.0"

        // Only include needed languages to reduce APK size
        resourceConfigurations += listOf("id", "en")
    }

    signingConfigs {
        create("release") {
            // CI environment: keystore from env vars (base64 decoded)
            val keystoreFilePath = System.getenv("KEYSTORE_FILE")
            val keystorePass = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasEnv = System.getenv("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD")

            if (keystoreFilePath != null && keystorePass != null && keyAliasEnv != null && keyPass != null) {
                // Full CI signing config from env vars
                storeFile = file(keystoreFilePath)
                storePassword = keystorePass
                keyAlias = keyAliasEnv
                keyPassword = keyPass
            } else if (file("release.keystore").exists()) {
                // Local development: use local keystore (gitignored)
                storeFile = file("release.keystore")
                storePassword = "melasti123"
                keyAlias = "melasti"
                keyPassword = "melasti123"
            }
            // If neither is available, release build falls back to debug signing
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing if keystore is available, otherwise fall back to debug
            signingConfig = if (signingConfigs.getByName("release").storeFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    // Split APKs by ABI for smaller downloads
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = false
        resValues = false
    }

    // Remove unused resources at build time
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    /* Compose BOM */
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    /* Compose */
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    /* Activity & Lifecycle */
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    /* Room */
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    /* Coroutines */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
