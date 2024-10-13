import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

import io.gitlab.arturbosch.detekt.Detekt

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaLanguageVersion: String by project

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.versions)
}

group = "org.ossreviewtoolkit.workbench"
version = "1.0.0"

repositories {
    google()
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven("https://androidx.dev/storage/compose-compiler/repository")
        }

        filter {
            includeGroup("androidx.compose.compiler")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://repo.gradle.org/gradle/libs-releases/")
        }

        filter {
            includeGroup("org.gradle")
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.bundles.ort)
    implementation(libs.bundles.richtext)
    implementation(libs.dataTableMaterial)
    implementation(libs.fileKit)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.kotlinxCoroutinesSwing)
    implementation(libs.log4jApiKotlin)
    implementation(libs.log4jApiToSlf4j)
    implementation(libs.logbackClassic)
    implementation(libs.moleculeRuntime)
    implementation(platform(libs.ortPackageConfigurationProviders))

    detektPlugins(libs.detektFormatting)
    detektPlugins(libs.ortDetektRules)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
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

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaLanguageVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

val maxKotlinJvmTarget = runCatching { JvmTarget.fromTarget(javaLanguageVersion) }
    .getOrDefault(enumValues<JvmTarget>().max())

tasks.withType<KotlinCompile> {
    val customCompilerArgs = listOf(
        "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
    )

    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.addAll(customCompilerArgs)
        jvmTarget = maxKotlinJvmTarget
    }
}

detekt {
    config.from(files("detekt.yml"))
    buildUponDefaultConfig = true
    basePath = rootProject.projectDir.path
    source.from(fileTree(".") { include("*.gradle.kts") })
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = maxKotlinJvmTarget.target

    reports {
        xml.required = false
        html.required = false
        txt.required = false
        sarif.required = true
    }
}

tasks.test {
    useJUnitPlatform()
}

compose {
    desktop {
        application {
            mainClass = "org.ossreviewtoolkit.workbench.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "ort-workbench"
                packageVersion = "1.0.0"

                val iconsRoot = project.file("src/main/resources/app-icon")

                macOS {
                    iconFile = iconsRoot.resolve("icon.icns")
                    jvmArgs("-Dapple.awt.application.appearance=system")
                }

                windows {
                    iconFile = iconsRoot.resolve("icon.ico")
                }

                linux {
                    iconFile = iconsRoot.resolve("icon.png")
                }
            }
        }
    }
}
