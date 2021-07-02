val quarkusVersion = "2.0.0.Final"

dependencies {
    api(project(":database-utils-core"))
    api("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")
}