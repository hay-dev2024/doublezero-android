import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.google.ksp)

}

// 1. local.properties 파일 읽기 로직
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.doublezero.feature_mypage"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // 2. BuildConfig에 String 필드 추가 (Key, Name, Value)
        // 코드에서 BuildConfig.WEB_CLIENT_ID 로 접근 가능해짐
        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: "\"MISSING_API_KEY\""
        )
    }

    // 3. BuildConfig 기능 활성화
    buildFeatures {
        compose = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}
kotlin{
    jvmToolchain(21)
}
dependencies {

    implementation(project(":shared"))
    implementation(project(":data"))

    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.bundles.compose.libraries)

    // Coil Compose 이미지 로더
    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.animation)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.play.services.auth)
}