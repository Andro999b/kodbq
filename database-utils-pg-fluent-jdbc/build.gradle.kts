val fluentjdbcVersion = "1.8.5"

dependencies {
    api(project(":database-utils-core"))
    api(project(":database-utils-pg"))
    implementation("org.codejargon:fluentjdbc:$fluentjdbcVersion")
}