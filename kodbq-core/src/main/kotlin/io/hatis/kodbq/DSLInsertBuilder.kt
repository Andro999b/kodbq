package io.hatis.kodbq

class DSLInsertBuilder(
    private val tableName: String,
    private val mode: SqlDialect
) {
    private var values: MutableList<Map<Column, Any?>> = mutableListOf()
    private var genKeys: Set<String> = emptySet()

    fun values(builderActions: DSLUpdateColumnsBuilder.() -> Unit) {
        val dslColumnsBuilder = DSLUpdateColumnsBuilder(mode)
        dslColumnsBuilder.builderActions()
        values.add(dslColumnsBuilder.columns)
    }

    fun generatedKeys(vararg keys: String) {
        genKeys = keys.toSet()
    }

    internal fun createInsertBuilder() = InsertBuilder(
        tableName = tableName,
        dialect = mode,
        values = values,
        generatedKeys = genKeys
    )
}