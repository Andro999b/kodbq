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

    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
