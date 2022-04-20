val fluentjdbcVersion = "1.8.5"

dependencies {
    api(project(":database-utils-core"))
    api("org.codejargon:fluentjdbc:$fluentjdbcVersion")
}