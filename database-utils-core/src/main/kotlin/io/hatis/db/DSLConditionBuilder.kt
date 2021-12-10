package io.hatis.db

open class DSLConditionBuilder(
    protected val mode: SqlMode,
    private val tableName: String? = null,
    protected val andJoint: And = And()
) {
    fun id(value: Any) {
        column("id", WhereOps.eq, value)
    }

    fun column(columnName: String, value: Any) {
        column(columnName, WhereOps.eq, value)
    }

    fun columns(vararg pairs: Pair<String, Any?>) {
        pairs.forEach { (columnName, value) ->
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun columns(map: Map<String, Any?>) {
        map.forEach { (columnName, value) ->
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun column(columnName: String, value: Collection<Any>) {
        column(columnName, WhereOps.`in`, value)
    }

    fun column(columnName: String, op: WhereOps, value: Any) {
        andJoint.parts.add(WhereColumn(Column(columnName, mode, tableName), op, value))
    }

    fun columnIsNull(columnName: String) {
        andJoint.parts.add(WhereColumnIsNull(Column(columnName, mode, tableName)))
    }

    fun columnNotNull(columnName: String) {
        andJoint.parts.add(WhereColumnIsNotNull(Column(columnName, mode, tableName)))
    }

    fun columnSql(columnName: String, actions: DSLColumnSqlGenerator.() -> Unit) {
        andJoint.parts.add(WhereGeneratedSql(Column(columnName, mode, tableName), actions))
    }
}