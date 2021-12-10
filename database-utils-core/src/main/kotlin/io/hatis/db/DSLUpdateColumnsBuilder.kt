package io.hatis.db

open class DSLUpdateColumnsBuilder(private val mode: SqlMode) {
    internal val columns: MutableMap<Column, Any?> = mutableMapOf()

    fun columns(params: Map<String, Any?>) {
        columns.putAll(params.mapKeys { (columnName, _) -> Column(columnName, mode) })
    }

    fun columns(vararg pairs: Pair<String, Any?>) {
        pairs.forEach { column(it.first, it.second) }
    }

    fun column(columnName: String, value: Any?) {
        columns[Column(columnName, mode)] = value
    }

    fun column(columnName: Enum<*>, value: Any?) {
        columns[Column(columnName.name, mode)] = value
    }

    fun columnSql(columnName: String, actions: DSLColumnSqlGenerator.() -> Unit) {
        column(columnName, DSLColumnSqlGenerator.CustomSqlPart(actions))
    }
}