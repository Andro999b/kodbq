val quarkusVersion = "2.8.0.Final"

dependencies {
    api(project(":database-utils-core"))
    api("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")
    implementation("org.slf4j:slf4j-api:1.7.36")
}