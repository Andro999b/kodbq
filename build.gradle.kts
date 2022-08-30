plugins {
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenCentral()
}

val releaseVersion = "1.0.0"

version = releaseVersion

subprojects {
    apply { plugin("kotlin") }

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

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }
}


