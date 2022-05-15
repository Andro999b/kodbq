plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

val mutinySqlVertexVersion = "2.21.0"

dependencies {
    api(project(":kodbq-core"))
    api("io.smallrye.reactive:smallrye-mutiny-vertx-jdbc-client:$mutinySqlVertexVersion")

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.apache.commons:commons-dbcp2:2.9.0")
    //pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
}