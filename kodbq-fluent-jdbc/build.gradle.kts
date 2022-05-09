val fluentjdbcVersion = "1.8.5"

dependencies {
    api(project(":kodbq-core"))
    api("org.codejargon:fluentjdbc:$fluentjdbcVersion")

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.apache.commons:commons-dbcp2:2.9.0")
    // pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")

    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

