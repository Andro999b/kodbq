package io.hatis.kodbq

class DSLInsertBuilder(
    private val table: Table,
    private val dialect: SqlDialect
) {
    private var values: MutableList<Map<Column, Any?>> = mutableListOf()
    private var genKeys: Set<Column> = emptySet()

    fun values(builderActions: DSLUpdateColumnsBuilder.() -> Unit) {
        val dslColumnsBuilder = DSLUpdateColumnsBuilder(table, dialect)
        dslColumnsBuilder.builderActions()
        values.add(dslColumnsBuilder.columns)
    }

    fun generatedKeys(vararg keys: ColumnDefinition) {
        keys.forEach {
            if(it.table != table)
                throw IllegalArgumentException("Column $it not belong table $table")
        }

        genKeys = keys.map { it.toColumn(dialect) }.toSet()
    }

    internal fun createInsertBuilder() = InsertBuilder(
        table = table,
        dialect = dialect,
        values = values,
        generatedKeys = genKeys
    )
}