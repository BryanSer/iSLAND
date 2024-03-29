import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
group = "com.github.bryanser"
version = "1.0-SNAPSHOT"


dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("com.onarandombox.multiversecore:multiverse-core:4.3.12")
    // https://mvnrepository.com/artifact/com.github.bryanser/BrAPI
    compileOnly("com.github.bryanser:BrAPI:Kt-1.0.82")

    implementation(project(":model"))
}

tasks {
    shadowJar {
        relocate("io.reactivex", "shadow.island.io.reactivex")
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