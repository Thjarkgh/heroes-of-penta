import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.devtools.ksp)
  //alias(libs.plugins.compose.compiler)
  //alias(libs.plugins.ethersAbigen)
//  id(libs.plugins.kotlin.parcelize.get().pluginId)
////  id(libs.plugins.kotlin.kapt.get().pluginId)
//  alias(libs.plugins.google.services)
//  alias(libs.plugins.firebase.crashlytics)
  // id("signing-config")
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
  compileSdk = 34
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
    val tikTokClientId = project.loadLocalProperty(
      path = "local.properties",
      propertyName = "tikTokClientId"
    )
    val tikTokClientSecret = project.loadLocalProperty(
      path = "local.properties",
      propertyName = "tikTokClientSecret"
    )
    val tikTokRedirectUrl = project.loadLocalProperty(
      path = "local.properties",
      propertyName = "tikTokRedirectUrl"
    )
    val dummyUserId = project.loadLocalProperty(
      path = "local.properties",
      propertyName = "dummyUserId"
    )
    val dummyUserToken = project.loadLocalProperty(
      path = "local.properties",
      propertyName = "dummyUserToken"
    )
    buildConfigField("String", "reownProjectId", reownProjectId)
    buildConfigField("String", "tikTokClientId", tikTokClientId)
    buildConfigField("String", "tikTokClientSecret", tikTokClientSecret)
    buildConfigField("String", "tikTokRedirectUrl", tikTokRedirectUrl)
    buildConfigField("int", "dummyUserId", dummyUserId)
    buildConfigField("String", "dummyUserToken", dummyUserToken)
    applicationId = "com.heroesofpenta"
    minSdk = 31
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
      kotlinOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
      }
    }
    debug {
      kotlinOptions {
        freeCompilerArgs = listOf("-Xdebug", "-Xcontext-receivers")
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    viewBinding = true
    compose = true
    buildConfig = true
  }
  composeOptions {
    //kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    kotlinCompilerExtensionVersion = "1.5.14"
    //kotlinCompilerExtensionVersion = "1.9.24" //libs.versions.composeCompiler.get()
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
  //buildToolsVersion = "34.0.1"
  ndkVersion = "27.0.12077973"
}

dependencies {
  implementation(libs.glide)
  // implementation(libs.androidx.navigation.compose)
//  implementation(libs.androidx.material3.android)
  ksp(libs.ksp)
  implementation(libs.retrofit)
  implementation(libs.converter.gson)
  implementation(libs.androidx.core)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.camera.view)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  //releaseImplementation(platform(libs.android.bom))
  implementation(libs.tiktok.open.sdk.core)
  implementation(libs.tiktok.open.sdk.auth)   // to use authorization api
  implementation(libs.tiktok.open.sdk.share)    // to use share api
  implementation(libs.androidx.browser) // to use chrome tab tiktok auth
  //implementation(libs.androidx.lifecycle.runtime.ktx)
  //implementation(libs.androidx.lifecycle.viewmodel.compose)
  //implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.palette)

  implementation(libs.koin.androidx.compose)
  implementation(libs.coil.compose)

//  implementation(libs.qrCodeGenerator)
//  implementation(platform(libs.androidx.compose.bom))
//  implementation(libs.androidx.compose.ui)
//  implementation(libs.androidx.compose.ui.tooling.preview)
//  implementation(libs.androidx.compose.material)
//  implementation(libs.androidx.compose.navigation)
//  implementation(libs.androidx.compose.lifecycle)
//  debugImplementation(libs.androidx.compose.ui.tooling)
//  debugImplementation(libs.androidx.compose.ui.test.manifest)
//  androidTestImplementation(libs.androidx.compose.ui.test.junit)
//  androidTestImplementation(libs.androidx.compose.navigation.testing)

//  implementation(libs.bundles.accompanist)
//
//  implementation(platform(libs.firebase.bom))
//  implementation(libs.bundles.firebase)

//  debugImplementation(project(":core:android"))
//  debugImplementation(project(":product:appkit"))

//  internalImplementation(project(":core:android"))
//  internalImplementation(project(":product:appkit"))

  implementation(platform(libs.reown.android.bom))
  implementation(libs.reown.android.core)
  implementation(libs.reown.appkit)

  //val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
  implementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(platform(libs.androidx.compose.bom))

  // Choose one of the following:
  // Material Design 3
  //implementation(libs.androidx.material3)
  // or Material Design 2
  implementation(libs.androidx.compose.material)
  // or skip Material Design and build directly on top of foundational components
  // implementation("androidx.compose.foundation:foundation")
  // or only import the main APIs for the underlying toolkit systems,
  // such as input and measurement/layout
  implementation(libs.androidx.compose.ui)

  // Android Studio Preview support
//  implementation(libs.androidx.ui.tooling.preview)
//  debugImplementation(libs.androidx.ui.tooling)
  implementation(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // UI Tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Optional - Included automatically by material, only add when you need
  // the icons but not the material library (e.g. when using Material3 or a
  // custom design system based on Foundation)
  // implementation("androidx.compose.material:material-icons-core")
  // Optional - Add full set of material icons
  // implementation("androidx.compose.material:material-icons-extended")
  // Optional - Add window size utils
  //implementation(libs.androidx.compose.adaptive)

  // Optional - Integration with activities
  //implementation(libs.androidx.activity.compose)
  // Optional - Integration with ViewModels
  implementation(libs.androidx.compose.lifecycle)
  // Optional - Integration with LiveData
  // implementation(libs.androidx.runtime.livedata)
  // Optional - Integration with RxJava
  //implementation("androidx.compose.runtime:runtime-rxjava2")
//  implementation(libs.androidx.compose.navigation)
  implementation(libs.androidx.compose.navigation)
  androidTestImplementation(libs.androidx.compose.navigation.testing)
    implementation(libs.androidx.compose.material.icons.core)
  // Define a BOM and its version
  implementation(libs.accompanist.navigation.material)
  implementation(libs.accompanist.permissions)
  //implementation(libs.ethers.bom)


  // (Optional) If you want to use Accompanist placeholders:
  implementation(libs.accompanist.placeholder.material)
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