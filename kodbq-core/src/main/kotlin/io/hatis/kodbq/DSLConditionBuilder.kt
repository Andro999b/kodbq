package io.hatis.kodbq

open class DSLConditionBuilder(protected val dialect: SqlDialect) {
    protected val andJoint: And = And()
    protected val orJoint: Or = Or()

    fun column(c: ColumnDefinition, value: Any) {
        andJoint.parts += (WhereColumn(c.toColunm(dialect), WhereOps.EQ, value, dialect))
    }

    fun column(c: ColumnDefinition, value: Collection<*>) {
        andJoint.parts += (WhereColumn(c.toColunm(dialect), WhereOps.IN, value, dialect))
    }

    fun column(c: ColumnDefinition): ColumnConditionBuilder {
        return ColumnConditionBuilder(andJoint.parts, dialect, c.toColunm(dialect))
    }
    fun columnIsNull(c: ColumnDefinition) {
        andJoint.parts += (WhereColumnIsNull(c.toColunm(dialect)))
    }

    fun columnNotNull(c: ColumnDefinition) {
        andJoint.parts += (WhereColumnIsNotNull(c.toColunm(dialect)))
    }


    fun columns(vararg pairs: Pair<ColumnDefinition, Any?>) {
        pairs.forEach { (columnName, value) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun columns(map: Map<ColumnDefinition, Any?>) {
        map.forEach { (columnName, value) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun native(actions: NativeSql.Generator.() -> String) {
        andJoint.parts += (WhereGeneratedSql(NativeSql(dialect, actions)))
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
        private val dialect: SqlDialect,
        private val column: Named
    ) {
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