plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hcmute.edu.vn.linhvalocvabao.selfalarmproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.linhvalocvabao.selfalarmproject"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.media)
    implementation(libs.androidx.work)
    implementation(libs.androidx.core)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.gson)
}