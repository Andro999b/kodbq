plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

val vertxVersion = "4.3.0"

dependencies {
    api(project(":kodbq-core"))
    api("io.vertx:vertx-sql-client:$vertxVersion")

    testImplementation(project(":kodbq-test-kit"))
    //pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
    testImplementation("io.vertx:vertx-pg-client:$vertxVersion")
    testImplementation("com.ongres.scram:client:2.1") // wtf?
    //mysql test
    testImplementation("org.testcontainers:mysql")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation("io.vertx:vertx-mysql-client:$vertxVersion")

}