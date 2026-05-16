import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "compose.project.wc.composewclibrary"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        androidResources {
            enable = true
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation
            implementation(libs.navigation.compose)

            // Material icons
            implementation(libs.material.icons.extended)

            // DataStore
            implementation(libs.datastore.preferences)

            // Okio
            implementation(libs.okio)

            // DateTime
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.slf4j.simple)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.nxzef.wc.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )
            // Automatically read version from AppConfig.kt
            val appConfigText = project.file("src/commonMain/kotlin/com/nxzef/wc/config/AppConfig.kt").readText()
            val versionRegex = """const val CURRENT_VERSION = "([^"]+)"""".toRegex()
            val versionFromCode = versionRegex.find(appConfigText)?.groupValues?.get(1) ?: "1.0.0"

            packageName = "WeddingClouds"
            packageVersion = versionFromCode
            description = "Photography CRM"
            copyright = "© 2026 The Wedding Clouds"
            vendor = "The Wedding Clouds"

            // jlink strips the JRE to only detected modules — explicitly include
            // jdk.unsupported so sun.misc.Unsafe is present (needed by DataStore/protobuf)
            // jdk.crypto.ec for TLS/HTTPS (Ktor connecting to the backend server)
            modules("jdk.unsupported", "jdk.crypto.ec")

            macOS {
                bundleID = "com.nxzef.wc"
                packageName = "WeddingClouds"
            }

            windows {
                menuGroup = "WeddingClouds"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                shortcut = true
                dirChooser = true
            }

            linux {
                packageName = "weddingclouds"
                debMaintainer = "noreply@weddingclouds.com"
                menuGroup = "Office"
                appCategory = "Office"
            }
        }
    }
}
