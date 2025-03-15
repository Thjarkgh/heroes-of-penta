pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven { url = java.net.URI("https://artifact.bytedance.com/repository/AwemeOpenSDK") }
    google()
    mavenLocal()
    mavenCentral()
    maven { url = java.net.URI("https://jitpack.io") }
    maven { url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    jcenter() // Warning: this repository is going to shut down soon
  }
}

rootProject.name = "Heroes of Penta"
include(":app")
