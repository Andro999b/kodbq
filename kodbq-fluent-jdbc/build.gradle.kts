plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

val fluentjdbcVersion = "1.8.5"

dependencies {
    api(project(":kodbq-core"))
    api("org.codejargon:fluentjdbc:$fluentjdbcVersion")

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.apache.commons:commons-dbcp2:2.9.0")
    // pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
}



