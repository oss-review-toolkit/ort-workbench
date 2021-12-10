import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ortVersion: String by project

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}

group = "org.ossreviewtoolkit.workbench"
version = "1.0"

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

    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.0.0")

    implementation("com.github.oss-review-toolkit.ort:analyzer:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:downloader:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:evaluator:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:reporter:$ortVersion")
    implementation("com.github.oss-review-toolkit.ort:scanner:$ortVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "org.ossreviewtoolkit.workbench.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ort-workbench"
            packageVersion = "1.0.0"
        }
    }
}
