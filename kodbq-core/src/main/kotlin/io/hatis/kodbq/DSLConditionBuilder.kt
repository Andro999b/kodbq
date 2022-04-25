package io.hatis.kodbq

open class DSLConditionBuilder(
    protected val dialect: SqlDialect,
    private val tableName: String?,
    protected val andJoint: And = And()
) {
    private val columnConditionBuilder = ColumnConditionBuilder(andJoint.parts, dialect)
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
        andJoint.parts += (WhereColumn(Column(columnName, dialect, tableName), op, value, dialect))
    }

    fun column(columnName: String): ColumnConditionBuilder {
        return columnConditionBuilder.apply {
            column = Column(columnName, dialect, tableName)
        }
    }

    fun columnIsNull(columnName: String) {
        andJoint.parts += (WhereColumnIsNull(Column(columnName, dialect, tableName)))
    }

    fun columnNotNull(columnName: String) {
        andJoint.parts += (WhereColumnIsNotNull(Column(columnName, dialect, tableName)))
    }

    fun native(columnName: String, actions: NativeSqlColumn.Generator.() -> String?) {
        andJoint.parts += (WhereGeneratedSql(NativeSqlColumn(Column(columnName, dialect, tableName), actions)))
    }

    class ColumnConditionBuilder(
        private val parts: MutableList<WherePart>,
        private val dialect: SqlDialect
    ) {
        internal lateinit var column: Column
        infix fun eq(value: Any) {
            parts += WhereColumn(column, WhereOps.EQ, value, dialect)
        }

        infix fun neq(value: Any) {
            parts += WhereColumn(column, WhereOps.NEQ, value, dialect)
        }

        infix fun gt(value: Any) {
            parts += WhereColumn(column, WhereOps.GT, value, dialect)
        }

        infix fun lt(value: Any) {
            parts += WhereColumn(column, WhereOps.LT, value, dialect)
        }

        infix fun gte(value: Any) {
            parts += WhereColumn(column, WhereOps.GTE, value, dialect)
        }

        infix fun lte(value: Any) {
            parts += WhereColumn(column, WhereOps.LTE, value, dialect)
        }

        infix fun like(value: String) {
            parts += WhereColumn(column, WhereOps.LIKE, value, dialect)
        }

        infix fun `in`(value: Collection<Any>) {
            parts += WhereColumn(column, WhereOps.IN, value, dialect)
        }

        fun isNull() {
            parts += WhereColumnIsNull(column)
        }

        fun notNull() {
            parts += WhereColumnIsNotNull(column)
        }
    }
}