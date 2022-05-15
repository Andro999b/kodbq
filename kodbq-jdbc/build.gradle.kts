plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

dependencies {
    api(project(":kodbq-core"))

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.apache.commons:commons-dbcp2:2.9.0")
    // pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
    //mssql test
    testImplementation("org.testcontainers:mssqlserver")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:9.4.1.jre11")
}