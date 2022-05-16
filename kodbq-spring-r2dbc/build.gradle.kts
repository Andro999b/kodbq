plugins {
    id("kodbq-testing")
    id("kodbq-publish")
}

repositories {
    maven { url = uri("https://repo.spring.io/libs-milestone") }
}

dependencies {
    api(project(":kodbq-core"))
    api("org.springframework:spring-r2dbc:5.3.19")
    implementation("io.r2dbc:r2dbc-spi:0.9.1.RELEASE")

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.testcontainers:r2dbc")
    // test pg
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
    testImplementation("org.postgresql:r2dbc-postgresql:0.9.1.RELEASE")
    //mysql test
    testImplementation("org.testcontainers:mysql")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation("com.github.jasync-sql:jasync-r2dbc-mysql:2.0.6")
    //mssql test
    testImplementation("org.testcontainers:mssqlserver")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:9.4.1.jre11")
    testImplementation("io.r2dbc:r2dbc-mssql:0.9.0.RELEASE")
}