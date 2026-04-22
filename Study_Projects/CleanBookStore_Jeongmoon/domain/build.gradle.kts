plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
}