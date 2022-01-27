import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val log4jVersion: String by project
val ortVersion: String by project
val richtextVersion: String by project

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1"
    id("com.github.ben-manes.versions") version "0.41.0"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
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

    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.0.1")

    implementation("com.github.oss-review-toolkit.ort:analyzer:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:downloader:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:evaluator:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:reporter:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:scanner:$ortVersion")

    implementation("com.halilibo.compose-richtext:richtext-commonmark:$richtextVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

tasks.test {
    useJUnitPlatform()
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

compose.desktop {
    application {
        mainClass = "org.ossreviewtoolkit.workbench.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ort-workbench"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("src/main/resources/app-icon")

            macOS {
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
            }

            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
            }

            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }
        }
    }
}

detekt {
    toolVersion = "1.19.0"
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    basePath = rootProject.projectDir.path
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(true)
    }
}
