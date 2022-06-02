package io.hatis.kodbq

open class DSLConditionBuilder(protected val dialect: SqlDialect) {
    protected val andJoint: And = And()
    protected val orJoint: Or = Or()

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

    infix fun Conditionable.eq(value: Any) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.EQ, value, dialect)
    }

    infix fun Conditionable.neq(value: Any) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.NEQ, value, dialect)
    }

    infix fun Conditionable.gt(value: Any) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.GT, value, dialect)
    }

    infix fun Conditionable.lt(value: Any) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.LT, value, dialect)
    }

    infix fun Conditionable.gte(value: Any) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.GTE, value, dialect)
    }

    infix fun Conditionable.lte(value: Any) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.LTE, value, dialect)
    }

    infix fun Conditionable.like(value: String) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.LIKE, value, dialect)
    }

    infix fun Conditionable.`in`(value: Collection<Any>) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.IN, value, dialect)
    }

    infix fun Conditionable.`in`(value: Array<*>) {
        andJoint.parts += WhereColumn(this.toConditionName(dialect), WhereOps.IN, value, dialect)
    }

    fun Conditionable.isNull() {
        andJoint.parts += WhereColumnIsNull(this.toConditionName(dialect))
    }

    fun Conditionable.notNull() {
        andJoint.parts += WhereColumnIsNotNull(this.toConditionName(dialect))
    }
}