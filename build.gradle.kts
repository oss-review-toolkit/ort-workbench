import io.gitlab.arturbosch.detekt.Detekt

import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val composePluginVersion: String by project
val detektPluginVersion: String by project

val log4jVersion: String by project
val ortVersion: String by project
val richtextVersion: String by project

plugins {
    kotlin("jvm")

    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.compose")
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

    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:$composePluginVersion")

    implementation("com.github.oss-review-toolkit.ort:analyzer:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:downloader:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:evaluator:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:reporter:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:scanner:$ortVersion")

    implementation("com.halilibo.compose-richtext:richtext-commonmark:$richtextVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    detektPlugins("com.github.oss-review-toolkit.ort:detekt-rules:$ortVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektPluginVersion")
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    val nonFinalQualifiers = listOf(
        "alpha", "b", "beta", "cr", "dev", "ea", "eap", "m", "milestone", "pr", "preview", "rc", "\\d{14}"
    ).joinToString("|", "(", ")")

    val nonFinalQualifiersRegex = Regex(".*[.-]$nonFinalQualifiers[.\\d-+]*", RegexOption.IGNORE_CASE)

    gradleReleaseChannel = "current"

    rejectVersionIf {
        candidate.version.matches(nonFinalQualifiersRegex)
    }
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
    toolVersion = detektPluginVersion
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
