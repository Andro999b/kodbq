plugins { java }

dependencies {
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.16.3"))
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
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