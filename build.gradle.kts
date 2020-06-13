plugins {
    java
    kotlin("jvm") version Config.Versions.Kotlin.kotlin
    kotlin("plugin.serialization") version Config.Versions.Kotlin.kotlin
}

kotlin.sourceSets {
    all {
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}

group = "com.petersamokhin.vksdk.audiotokenfetcher"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", Config.Versions.Kotlin.kotlin))

    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-runtime", Config.Versions.Kotlin.serialization)
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-protobuf", Config.Versions.Kotlin.serialization)

    implementation("io.ktor", "ktor-client-core-jvm", Config.Versions.ktor)
}