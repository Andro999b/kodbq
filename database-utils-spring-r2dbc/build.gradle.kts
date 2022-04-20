val springVersion = "5.3.17"

repositories {
    maven { url = uri("https://repo.spring.io/libs-milestone") }
}

dependencies {
    api(project(":database-utils-core"))
    api("org.springframework:spring-r2dbc:$springVersion")
}