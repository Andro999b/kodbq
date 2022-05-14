package io.hatis.kodbq

interface SqlBuilder {
    val dialect: SqlDialect
    var buildOptions: BuildOptions
    fun buildSqlAndParams(): Pair<String, List<Any?>>
}