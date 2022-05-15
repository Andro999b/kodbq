plugins { java }

val testcontainersVersion = "1.16.3"
val log4j2Version = "2.17.2"

dependencies {
    testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
}

sourceSets {
    test {
        resources {
            srcDir("../test-resources")
        }
    }
}

tasks.withType<Test> {
    testLogging.showStandardStreams = true
    useJUnitPlatform()
}