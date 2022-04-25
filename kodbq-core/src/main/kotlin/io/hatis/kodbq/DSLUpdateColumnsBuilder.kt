package io.hatis.kodbq

open class DSLUpdateColumnsBuilder(private val dialect: SqlDialect) {
    internal val columns: MutableMap<Column, Any?> = mutableMapOf()

    fun columns(params: Map<String, Any?>) {
        columns.putAll(params.mapKeys { (columnName, _) -> Column(columnName, dialect) })
    }

    fun columns(vararg pairs: Pair<String, Any?>) {
        pairs.forEach { column(it.first, it.second) }
    }

    fun column(columnName: String, value: Any?) {
        columns[Column(columnName, dialect)] = value
    }

    fun native(columnName: String, actions: NativeSqlColumn.Generator.() -> String?) {
        val column = Column(columnName, dialect)
        columns[column] = NativeSqlColumn(column, actions)
    }
}