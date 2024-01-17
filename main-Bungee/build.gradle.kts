import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
group = "com.github.bryanser"
version = "1.0-SNAPSHOT"

repositories {
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.md-5:bungeecord-api:1.12-SNAPSHOT")
    implementation(project(":model"))
}

tasks {
    shadowJar{
        relocate("io.reactivex.rxjava2", "shadow.island.io.reactivex.rxjava2")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}