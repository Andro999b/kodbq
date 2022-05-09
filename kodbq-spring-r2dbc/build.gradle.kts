val springVersion = "5.3.19"

repositories {
    maven { url = uri("https://repo.spring.io/libs-milestone") }
}

dependencies {
    api(project(":kodbq-core"))
    api("org.springframework:spring-r2dbc:$springVersion")

    testImplementation(project(":kodbq-test-kit"))
    testImplementation("org.testcontainers:r2dbc")
    // test pg
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql:42.3.4")
    testImplementation("io.r2dbc:r2dbc-postgresql:0.8.12.RELEASE")
}

sourceSets {
    test {
        resources {
            srcDir("../sql")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
