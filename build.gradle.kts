plugins {
    kotlin("jvm") version "1.4.30"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

subprojects {
    apply { plugin("kotlin") }
    apply { plugin("maven-publish") }

    version = "0.0.1"

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Andro999/kotlin-database-utils")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITPKG_USER")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITPKG_TOKEN")
                }
            }
        }
        publications {
            create<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}




