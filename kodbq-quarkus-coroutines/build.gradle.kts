val coroutineVersion = "1.6.1"
val mutinyVersion = "1.4.0"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("io.smallrye.reactive:mutiny-kotlin:$mutinyVersion")

    api(project(":kodbq-quarkus-reactive"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
}