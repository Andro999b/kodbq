val quarkusVersion = "1.12.0.Final"

dependencies {
    api(project(":database-utils-core"))
    api(project(":database-utils-pg"))
    implementation("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")
}