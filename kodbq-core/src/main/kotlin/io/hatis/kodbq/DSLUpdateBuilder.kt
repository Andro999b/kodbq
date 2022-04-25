package io.hatis.kodbq

class DSLUpdateBuilder(private val dialect: SqlDialect) {
    private lateinit var dslColumnsBuilder: DSLUpdateColumnsBuilder
    private var dslConditionBuilder: DSLUpdateConditionBuilder? = null

    fun set(builderActions: DSLUpdateColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLUpdateColumnsBuilder(dialect)
        dslColumnsBuilder.builderActions()
    }

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLUpdateConditionBuilder(dialect)
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createUpdateBuilder(tableName: String) = UpdateBuilder(
        tableName = tableName,
        columns = dslColumnsBuilder.columns,
        where = dslConditionBuilder?.createWhereCondition(),
        dialect = dialect
    )
}