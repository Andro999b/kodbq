package io.hatis.kodbq

class QueryBuilder(private val nativeSql: NativeSql) : AbstractSqlBuilder() {
    override val dialect: SqlDialect = nativeSql.dialect
    override fun buildSqlAndParams(): Pair<String, List<Any?>> {
        val outParams = mutableListOf<Any?>()
        val sql = nativeSql.generate(outParams = outParams, paramPlaceholder = buildOptions.paramPlaceholder)

        return sql to outParams
    }
}