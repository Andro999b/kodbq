val coroutineVersion = "1.5.0"
val mutinyVersion = "1.1.2"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutineVersion}")
    implementation("io.smallrye.reactive:mutiny-kotlin:${mutinyVersion}")

    api(project(":database-utils-quarkus-reactive"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
}