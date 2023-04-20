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
    implementation(project(":shared"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("rdt.MainKt")
}
