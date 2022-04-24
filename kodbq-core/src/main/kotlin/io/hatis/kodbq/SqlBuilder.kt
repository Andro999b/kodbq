package io.hatis.kodbq

interface SqlBuilder {
    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>>
}