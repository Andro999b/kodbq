package io.hatis.kodbq

data class BuildOptions(
    val expandIn: Boolean = true
)

val defaultBuildOptions = BuildOptions()

abstract class AbstractSqlBuilder: SqlBuilder {
    var buildOptions: BuildOptions = defaultBuildOptions
}