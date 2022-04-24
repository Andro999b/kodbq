val fluentjdbcVersion = "1.8.5"

dependencies {
    api(project(":kodbq-core"))
    api("org.codejargon:fluentjdbc:$fluentjdbcVersion")
}