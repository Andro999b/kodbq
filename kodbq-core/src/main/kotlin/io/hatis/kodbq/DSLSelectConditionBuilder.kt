package io.hatis.kodbq

class DSLSelectConditionBuilder(dialect: SqlDialect) : DSLConditionBuilder(dialect) {

    fun or(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(dialect)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(dialect)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}