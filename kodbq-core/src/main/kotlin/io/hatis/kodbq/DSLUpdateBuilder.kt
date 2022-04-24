package io.hatis.kodbq

class DSLUpdateBuilder(private val mode: SqlDialect) {
    private lateinit var dslColumnsBuilder: DSLUpdateColumnsBuilder
    private var dslConditionBuilder: DSLUpdateConditionBuilder? = null

    fun set(builderActions: DSLUpdateColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLUpdateColumnsBuilder(mode)
        dslColumnsBuilder.builderActions()
    }

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLUpdateConditionBuilder(mode)
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createUpdateBuilder(tableName: String) = UpdateBuilder(
        tableName = tableName,
        columns = dslColumnsBuilder.columns,
        where = dslConditionBuilder?.createWhereCondition(),
        dialect = mode
    )
}