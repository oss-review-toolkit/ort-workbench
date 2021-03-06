import io.gitlab.arturbosch.detekt.Detekt

import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.versionCatalogUpdate)
    alias(libs.plugins.versions)
}

group = "org.ossreviewtoolkit.workbench"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")

    exclusiveContent {
        forRepository {
            maven("https://repo.gradle.org/gradle/libs-releases/")
        }

        filter {
            includeGroup("org.gradle")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://repo.eclipse.org/content/repositories/sw360-releases/")
        }

        filter {
            includeGroup("org.eclipse.sw360")
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.bundles.log4j)
    implementation(libs.bundles.ort)
    implementation(libs.bundles.richtext)
    implementation(libs.composeMaterialIconsExtendedDesktop)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.kotlinxCoroutinesSwing)

    detektPlugins(libs.detektFormatting)
    detektPlugins(libs.ortDetektRules)
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    gradleReleaseChannel = "current"
    outputFormatter = "json"

    val nonFinalQualifiers = listOf(
        "alpha", "b", "beta", "cr", "dev", "ea", "eap", "m", "milestone", "pr", "preview", "rc", "\\d{14}"
    ).joinToString("|", "(", ")")

    val nonFinalQualifiersRegex = Regex(".*[.-]$nonFinalQualifiers[.\\d-+]*", RegexOption.IGNORE_CASE)

    rejectVersionIf {
        candidate.version.matches(nonFinalQualifiersRegex)
    }
}

versionCatalogUpdate {
    // Keep the custom sorting / grouping.
    sortByKey.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

detekt {
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    basePath = rootProject.projectDir.path
    source.from(fileTree(".") { include("*.gradle.kts") })
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "org.ossreviewtoolkit.workbench.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ort-workbench"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("src/main/resources/app-icon")

            macOS {
                iconFile.set(iconsRoot.resolve("icon.icns"))
                jvmArgs("-Dapple.awt.application.appearance=system")
            }

            windows {
                iconFile.set(iconsRoot.resolve("icon.ico"))
            }

            linux {
                iconFile.set(iconsRoot.resolve("icon.png"))
            }
        }
    }
}
