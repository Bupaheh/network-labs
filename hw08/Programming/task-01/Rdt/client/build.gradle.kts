plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    application
}

kotlin {
    jvmToolchain(8)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":shared"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("rdt.MainKt")
}
