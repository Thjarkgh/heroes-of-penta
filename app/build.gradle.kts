// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.devtools.ksp) apply false
  //alias(libs.plugins.compose.compiler) apply false
  //alias(libs.plugins.ethers.abigen) apply false
}

allprojects {
  repositories {
    google()
    mavenLocal()
    mavenCentral()
    maven { url = java.net.URI("https://jitpack.io") }
    maven { url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = java.net.URI("https://artifact.bytedance.com/repository/AwemeOpenSDK") }
    jcenter() // Warning: this repository is going to shut down soon
  }
}