import org.jetbrains.kotlin.ir.backend.js.compile
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    //alias(libs.plugins.ethersAbigen)
}

//kotlin {
//  jvmToolchain(11)
//}
// default values
//ethersAbigen {
//  // set by default
//  directorySource("src/main/abi")
//
//  // set by default
//  outputDir = "generated/source/ethers/main/kotlin"
//
//  // default is empty map
//  functionRenames.putAll(
//    mapOf(
//      "approve" to "approveTokens",
//      "transferFrom" to "transferTokensFrom",
//    )
//  )
//
//  // by default, it supports reading Foundry, Hardhat, and Etherscan artifacts
//  abiReader { uri ->
//    // read JsonAbi from uri
//  }
//}
android {
    namespace = "com.heroesofpenta"
    compileSdk = 35
    lint {
        checkReleaseBuilds = false
    }
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {

        val reownProjectId = project.loadLocalProperty(
            path = "local.properties",
            propertyName = "reownProjectId",
        )
        buildConfigField("String", "reownProjectId", reownProjectId)
        applicationId = "com.heroesofpenta"
        minSdk = 28
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "35.0.1"
    ndkVersion = "27.0.12077973"
}

dependencies {
    implementation(libs.glide)
    ksp(libs.ksp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.android.bom))
    implementation(libs.android.core)
    implementation(libs.appkit)
  // Define a BOM and its version
  //implementation(libs.ethers.bom)

  // Define any required artifacts without version
//  implementation(libs.ethers.abi)
//  implementation(libs.ethers.core)
//  implementation(libs.ethers.providers)
//  implementation(libs.ethers.signers)
}

fun Project.loadLocalProperty(
    path: String,
    propertyName: String,
): String {
    val localProperties = Properties()
    val localPropertiesFile = project.rootProject.file(path)
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
        return localProperties.getProperty(propertyName)
    } else {
        throw GradleException("can not find property : $propertyName")
    }

}