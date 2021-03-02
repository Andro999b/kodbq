plugins {
    kotlin("jvm") version "1.4.30"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

repositories {
    jcenter()
    mavenCentral()
}


val releaseVersion = "0.0.8"
version = releaseVersion

subprojects {
    apply { plugin("kotlin") }
    apply { plugin("maven-publish") }
    apply { plugin("com.jfrog.bintray") }

    group = "io.hatis"
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
        publications {
            create<MavenPublication>("bintray") {
                from(components["java"])
            }
        }
    }

    bintray {
        user = project.findProperty("bintray.user") as String? ?: System.getenv("BINTRAY_USER")
        key = project.findProperty("bintray.key") as String? ?: System.getenv("BINTRAY_KEy")
        publish = true

        setPublications("bintray")

        pkg.apply {
            repo = "maven"
            githubRepo = "Andro999b/kotlin-database-utils"
            vcsUrl = "https://github.com/Andro999b/kotlin-database-utils"
            name = project.name
            setLicenses("MIT")

            version.apply {
                name = project.version.toString()
            }
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}

tasks.bintrayPublish {
    enabled = false
}

tasks.bintrayUpload {
    enabled = false
}



