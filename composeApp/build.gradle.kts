plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        compileSdk { version = release(36) }
        namespace = "com.juanpvivas.aichatjp.composeapp"
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {}
}
