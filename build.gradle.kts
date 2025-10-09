import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.10"
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "0.1.0"
application {
    mainClass.set("com.example.chess.MainKt")
}

repositories { mavenCentral() }

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.6")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("kotlin-chess")
    archiveClassifier.set("")
    archiveVersion.set(version.toString())
}

kotlin { jvmToolchain { (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17)) } }