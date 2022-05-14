package io.hatis.kodbq

abstract class AbstractSqlBuilder: SqlBuilder {
    override var buildOptions: BuildOptions = defaultBuildOptions
}