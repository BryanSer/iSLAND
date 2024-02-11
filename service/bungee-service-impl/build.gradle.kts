import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

group = "com.github.bryanser"
version = "1.0-SNAPSHOT"

repositories {
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":model"))
    implementation(project(":service:api"))
    compileOnly("net.md-5:bungeecord-api:1.12-SNAPSHOT")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

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