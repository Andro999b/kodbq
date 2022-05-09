val mutinySqlVertexVersion = "2.21.0"

dependencies {
    api(project(":kodbq-core"))
    api("io.smallrye.reactive:smallrye-mutiny-vertx-sql-client:$mutinySqlVertexVersion")

    testImplementation(project(":kodbq-test-kit"))
    //pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
    testImplementation("io.smallrye.reactive:smallrye-mutiny-vertx-jdbc-client:$mutinySqlVertexVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}