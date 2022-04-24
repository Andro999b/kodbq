val springVersion = "5.3.17"

repositories {
    maven { url = uri("https://repo.spring.io/libs-milestone") }
}

dependencies {
    api(project(":kodbq-core"))
    api("org.springframework:spring-jdbc:$springVersion")
}