package io.hatis.db

interface SqlBuilder {
    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>>
}