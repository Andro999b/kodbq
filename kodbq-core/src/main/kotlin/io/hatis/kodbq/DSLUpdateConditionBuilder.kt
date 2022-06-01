package io.hatis.kodbq

class DSLUpdateConditionBuilder(dialect: SqlDialect) : DSLConditionBuilder(dialect) {
    fun or(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val builder = DSLUpdateConditionBuilder(dialect)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val builder = DSLUpdateConditionBuilder(dialect)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}