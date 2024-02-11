import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

group = "org.example"
version = "unspecified"


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("com.onarandombox.multiversecore:multiverse-core:4.3.12")
    implementation(project(":model"))
    implementation(project(":service:bungee-service-impl"))
    implementation(project(":service:api"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    // https://mvnrepository.com/artifact/com.github.bryanser/BrAPI
    compileOnly("com.github.bryanser:BrAPI:Kt-1.0.82")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
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