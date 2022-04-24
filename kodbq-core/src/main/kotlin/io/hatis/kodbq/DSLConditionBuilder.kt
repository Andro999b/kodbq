package io.hatis.kodbq

open class DSLConditionBuilder(
    protected val mode: SqlDialect,
    private val tableName: String? = null,
    protected val andJoint: And = And()
) {
    fun id(value: Any) {
        column("id", WhereOps.EQ, value)
    }

    fun column(columnName: String, value: Any) {
        column(columnName, WhereOps.EQ, value)
    }

    fun columns(vararg pairs: Pair<String, Any?>) {
        pairs.forEach { (columnName, value) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun columns(map: Map<String, Any?>) {
        map.forEach { (columnName, value) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun column(columnName: String, value: Collection<Any>) {
        column(columnName, WhereOps.IN, value)
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

    fun native(columnName: String, actions: NativeSqlColumn.Generator.() -> String) {
        andJoint.parts.add(WhereGeneratedSql(NativeSqlColumn(Column(columnName, mode, tableName), actions)))
    }
}