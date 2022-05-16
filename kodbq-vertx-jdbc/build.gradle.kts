plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

val vertxVersion = "4.2.7"

dependencies {
    api(project(":kodbq-core"))
    api("io.vertx:vertx-jdbc-client:$vertxVersion")

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.apache.commons:commons-dbcp2:2.9.0")
    //pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
}