plugins {
    kotlin("jvm") version "1.6.20"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val testcontainersVersion = "1.16.3"
val releaseVersion = "1.0.0"

version = releaseVersion

subprojects {
    val isNotTestKit = !name.contains("test")

    apply { plugin("kotlin") }
    apply { plugin("maven-publish") }

    group = "com.github.Andro999b.kotlin-database-utils"
    version = releaseVersion

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.javaParameters = true
    }

    if(isNotTestKit) { // exclude test project
        publishing {
            publications {
                register<MavenPublication>("gpr") {
                    from(components["java"])
                }
            }
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    }
}


