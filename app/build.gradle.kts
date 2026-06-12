import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

/** Deve ser igual a MOBILE_API_KEY no backend. */
val rodoflowApiKey =
    localProperties.getProperty("RODOFLOW_API_KEY")
        ?: "rodoflow-dev-mobile-key-troque-em-producao"

/** Produção. Sobrescreva em local.properties com API_BASE_URL para dev local. */
val defaultApiBaseUrl = "https://api.rodoflow.net.br/"

fun normalizeApiBaseUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return defaultApiBaseUrl
    return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
}

val rodoflowApiBaseUrl = normalizeApiBaseUrl(
    localProperties.getProperty("API_BASE_URL") ?: defaultApiBaseUrl,
)

android {
    namespace = "com.example.rodoflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.rodoflow"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "RODOFLOW_API_KEY",
            "\"${rodoflowApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
        buildConfigField(
            "String",
            "RODOFLOW_API_BASE_URL",
            "\"${rodoflowApiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias").orEmpty()
                keyPassword = keystoreProperties.getProperty("keyPassword").orEmpty()
                val storePath = keystoreProperties.getProperty("storeFile").orEmpty()
                if (storePath.isNotEmpty()) {
                    storeFile = rootProject.file(storePath)
                }
                storePassword = keystoreProperties.getProperty("storePassword").orEmpty()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                null
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.coil.compose)
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}