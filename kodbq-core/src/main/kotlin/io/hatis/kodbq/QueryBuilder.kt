package io.hatis.kodbq

class QueryBuilder(private val nativeSql: NativeSql) : SqlBuilder {
    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val outParams = mutableListOf<Any?>()
        val sql = nativeSql.generate(outParams = outParams, paramPlaceholder = paramPlaceholder)

        return sql to outParams
    }
}