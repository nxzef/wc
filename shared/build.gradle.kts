plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    
    val skipMobile = System.getenv("SKIP_MOBILE") == "true"
    if (!skipMobile) {
        id("com.android.kotlin.multiplatform.library")
    }
}

kotlin {
    jvmToolchain(17)
    jvm()

    val skipMobile = System.getenv("SKIP_MOBILE") == "true"
    if (!skipMobile) {
        iosArm64()
        iosSimulatorArm64()
        
        // Apply Android configuration from a Groovy script to avoid KTS 
        // compilation errors when the Android plugin is missing.
        apply(from = "android-config.gradle")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
