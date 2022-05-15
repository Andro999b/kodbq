plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

val springVersion = "5.3.19"

repositories {
    maven { url = uri("https://repo.spring.io/libs-milestone") }
}

dependencies {
    api(project(":kodbq-core"))
    implementation("org.springframework:spring-jdbc:$springVersion")

    testImplementation(project(":kodbq-test-kit"))
    // pg test
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
    //mysql test
    testImplementation("org.testcontainers:mysql")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    //mssql test
    testImplementation("org.testcontainers:mssqlserver")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:9.4.1.jre11")
}