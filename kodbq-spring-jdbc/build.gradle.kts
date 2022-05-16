plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

repositories {
    maven { url = uri("https://repo.spring.io/libs-milestone") }
}

dependencies {
    api(project(":kodbq-core"))
    implementation("org.springframework:spring-jdbc:5.3.19")

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