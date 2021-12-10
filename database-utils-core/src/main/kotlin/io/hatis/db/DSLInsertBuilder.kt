package io.hatis.db

class DSLInsertBuilder(
    private val tableName: String,
    private val mode: SqlMode
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
        mode = mode,
        values = values,
        generatedKeys = genKeys
    )
}