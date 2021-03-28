val quarkusVersion = "1.13.0.Final"

dependencies {
    api(project(":database-utils-core"))
    api("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")
}