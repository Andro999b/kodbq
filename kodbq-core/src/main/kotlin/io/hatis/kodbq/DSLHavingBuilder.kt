package io.hatis.kodbq

class DSLHavingBuilder(dialect: SqlDialect): DSLHavingFunctionBuilder(dialect) {

    fun or(builderActions: DSLHavingBuilder.() -> Unit) {
        val builder = DSLHavingBuilder(dialect)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLHavingBuilder.() -> Unit) {
        val builder = DSLHavingBuilder(dialect)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}