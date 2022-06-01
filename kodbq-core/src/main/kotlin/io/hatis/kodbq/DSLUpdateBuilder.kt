package io.hatis.kodbq

class DSLUpdateBuilder(private val table: Table, private val dialect: SqlDialect) {
    private lateinit var dslColumnsBuilder: DSLUpdateColumnsBuilder
    private var dslConditionBuilder: DSLUpdateConditionBuilder? = null

    fun set(builderActions: DSLUpdateColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLUpdateColumnsBuilder(table, dialect)
        dslColumnsBuilder.builderActions()
    }

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLUpdateConditionBuilder(dialect)
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createUpdateBuilder() = UpdateBuilder(
        table = table,
        columns = dslColumnsBuilder.columns,
        where = dslConditionBuilder?.createWhereCondition(),
        dialect = dialect
    )
}