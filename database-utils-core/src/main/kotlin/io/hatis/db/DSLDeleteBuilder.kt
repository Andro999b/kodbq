package io.hatis.db

class DSLDeleteBuilder(
    private val tableName: String,
    private val mode: SqlMode
) {
    private lateinit var dslConditionBuilder: DSLUpdateConditionBuilder

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        dslConditionBuilder = DSLUpdateConditionBuilder(mode)
        dslConditionBuilder.builderActions()
    }

    internal fun createDeleteBuilder() = DeleteBuilder(
        tableName = tableName,
        where = dslConditionBuilder.createWhereCondition(),
        mode = mode
    )
}