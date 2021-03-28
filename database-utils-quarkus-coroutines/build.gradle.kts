val coroutineVersion = "1.4.2"
val mutinyVersion = "0.14.0"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutineVersion}")
    implementation("io.smallrye.reactive:mutiny-kotlin:${mutinyVersion}")

    api(project(":database-utils-quarkus-reactive"))
}