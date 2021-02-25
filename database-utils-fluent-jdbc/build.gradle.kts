val fluentjdbcVersion = "1.8.5"

dependencies {
    api(project(":database-utils-core"))
    implementation("org.codejargon:fluentjdbc:$fluentjdbcVersion")
}