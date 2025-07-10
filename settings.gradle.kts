pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    // Gradle cannot access the version catalog from here, so hard-code the dependency.
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "ort-workbench"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
       create("ortLibs") {
           from("org.ossreviewtoolkit:version-catalog:62.2.0")
       }
    }
}
