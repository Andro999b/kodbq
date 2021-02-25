val quarkusVersion = "1.12.0.Final"

dependencies {
    api(project(":database-utils-core"))
    implementation("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")
}