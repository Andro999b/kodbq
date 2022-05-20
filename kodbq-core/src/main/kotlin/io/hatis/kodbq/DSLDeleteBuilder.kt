package io.hatis.kodbq

class DSLDeleteBuilder(
    private val tableName: String,
    private val dialect: SqlDialect
) {
    private var dslConditionBuilder: DSLUpdateConditionBuilder? = null

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLUpdateConditionBuilder(dialect)
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createDeleteBuilder() = DeleteBuilder(
        tableName = tableName,
        where = dslConditionBuilder?.createWhereCondition(),
        dialect = dialect
    )
}