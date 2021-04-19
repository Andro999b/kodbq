plugins {
    kotlin("jvm") version "1.4.30"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val releaseVersion = "0.0.29"
version = releaseVersion

subprojects {
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

    publishing {
//        repositories {
//            maven {
//                name = "GitHubPackages"
//                url = uri("https://maven.pkg.github.com/Andro999b/kotlin-database-utils")
//                credentials {
//                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITPKG_USER")
//                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITPKG_TOKEN")
//                }
//            }
//        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}


