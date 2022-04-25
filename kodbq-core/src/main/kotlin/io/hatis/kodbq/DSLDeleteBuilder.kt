package io.hatis.kodbq

class DSLDeleteBuilder(
    private val tableName: String,
    private val dialect: SqlDialect
) {
    private lateinit var dslConditionBuilder: DSLUpdateConditionBuilder

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        dslConditionBuilder = DSLUpdateConditionBuilder(dialect)
        dslConditionBuilder.builderActions()
    }

    internal fun createDeleteBuilder() = DeleteBuilder(
        tableName = tableName,
        where = dslConditionBuilder.createWhereCondition(),
        dialect = dialect
    )
}