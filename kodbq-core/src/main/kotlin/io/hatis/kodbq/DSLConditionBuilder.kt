package io.hatis.kodbq

open class DSLConditionBuilder(
    protected val dialect: SqlDialect,
    protected val tableName: String?,
    protected var andJoint: And
) {
    protected val columnConditionBuilder = ColumnConditionBuilder(andJoint.parts, dialect)
    protected val orJoint: Or = Or()

    fun id(value: Any) {
        column("id", value)
    }

    fun column(columnName: String, value: Any) {
        andJoint.parts += (WhereColumn(Column(columnName, dialect, tableName), WhereOps.EQ, value, dialect))
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

    fun column(columnName: String, value: Collection<*>) {
        andJoint.parts += (WhereColumn(Column(columnName, dialect, tableName), WhereOps.IN, value, dialect))
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

    fun native(actions: NativeSql.Generator.() -> String) {
        andJoint.parts += (WhereGeneratedSql(NativeSql(dialect, tableName, actions)))
    }

    fun createWhereCondition(): WherePart =
        if (orJoint.parts.isEmpty()) {
            andJoint
        } else {
            orJoint.parts.add(0, andJoint)
            orJoint
        }

    class ColumnConditionBuilder(
        private val parts: MutableList<WherePart>,
        private val dialect: SqlDialect
    ) {
        internal lateinit var column: Named
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

        infix fun inArray(value: Array<*>) {
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