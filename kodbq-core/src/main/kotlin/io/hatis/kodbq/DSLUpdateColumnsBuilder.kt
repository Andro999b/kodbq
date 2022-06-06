package io.hatis.kodbq

open class DSLUpdateColumnsBuilder(
    private val table: Table,
    private val dialect: SqlDialect
) {
    internal val columns: MutableMap<Column, Any?> = mutableMapOf()

    infix fun ColumnDefinition.to(value: Any?) {
        if(this.table != table)
            throw IllegalArgumentException("Column $this not belong table $table")

        columns[Column(this.name, dialect)] = value
    }

    fun native(cd: ColumnDefinition, actions: NativeSqlColumn.Generator.() -> String?) {
        if(cd.table != table)
            throw IllegalArgumentException("Column $cd not belong table $table")

        val column = Column(cd.name, dialect)
        columns[column] = NativeSqlColumn(column, actions)
    }
}