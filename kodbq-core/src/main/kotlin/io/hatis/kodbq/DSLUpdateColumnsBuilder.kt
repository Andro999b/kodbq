package io.hatis.kodbq

open class DSLUpdateColumnsBuilder(
    private val table: Table,
    private val dialect: SqlDialect
) {
    internal val columns: MutableMap<Column, Any?> = mutableMapOf()

    fun columns(cds: Map<ColumnDefinition, Any?>) {
        cds.forEach { column(it.key, it.value) }
    }

    fun columns(vararg cds: Pair<ColumnDefinition, Any?>) {
        cds.forEach { column(it.first, it.second) }
    }

    fun column(cd: ColumnDefinition, value: Any?) {
        if(cd.table != table)
            throw IllegalArgumentException("Column not belong table")
        
        columns[Column(cd.name, dialect)] = value
    }

    fun native(cd: ColumnDefinition, actions: NativeSqlColumn.Generator.() -> String?) {
        if(cd.table != table)
            throw IllegalArgumentException("Column not belong table")

        val column = Column(cd.name, dialect)
        columns[column] = NativeSqlColumn(column, actions)
    }
}