plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.deepsource"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.deepsource"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "1.4"
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${providers.gradleProperty("GOOGLE_WEB_CLIENT_ID").orElse("").get()}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${providers.gradleProperty("FIREBASE_API_KEY").orElse("").get()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += "backend"
    productFlavors {
        create("local") {
            dimension = "backend"
            isDefault = true
            buildConfigField("boolean", "USE_FIREBASE", "false")
        }
        create("cloud") {
            dimension = "backend"
            buildConfigField("boolean", "USE_FIREBASE", "true")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    "cloudImplementation"("androidx.credentials:credentials:1.5.0")
    "cloudImplementation"("androidx.credentials:credentials-play-services-auth:1.5.0")
    "cloudImplementation"("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
