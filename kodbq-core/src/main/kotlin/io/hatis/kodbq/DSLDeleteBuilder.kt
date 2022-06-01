package io.hatis.kodbq

class DSLDeleteBuilder(
    private val table: Table,
    private val dialect: SqlDialect
) {
    private var dslConditionBuilder: DSLUpdateConditionBuilder? = null

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLUpdateConditionBuilder(dialect)
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createDeleteBuilder() = DeleteBuilder(
        table = table,
        where = dslConditionBuilder?.createWhereCondition(),
        dialect = dialect
    )
}