val kotestVersion = "5.3.0"

dependencies {
    api(project(":kodbq-core"))
    api("io.kotest:kotest-runner-junit5:$kotestVersion")
    api("io.kotest:kotest-assertions-core:$kotestVersion")
    api("io.kotest:kotest-framework-datatest:$kotestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}