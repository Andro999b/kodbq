val kotestVersion = "5.2.2"
val testcontainersVersion = "1.16.3"

dependencies {
    api(project(":kodbq-core"))
//    implementation("org.testcontainers:testcontainers:$testcontainersVersion")
    implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
    implementation("io.kotest:kotest-framework-datatest:$kotestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}