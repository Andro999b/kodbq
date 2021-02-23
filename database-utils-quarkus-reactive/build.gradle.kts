val quarkusVersion = "1.11.3.Final"

dependencies {
    implementation(project(":database-utils-core"))
    implementation(project(":database-utils-pg"))
    implementation("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")
}