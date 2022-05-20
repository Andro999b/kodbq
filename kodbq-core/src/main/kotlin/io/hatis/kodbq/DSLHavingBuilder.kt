package io.hatis.kodbq

class DSLHavingBuilder(dialect: SqlDialect, tableName: String? = null) :
    DSLHavingFunctionBuilder(dialect, tableName) {

    fun table(tableOrAliasName: String, builderActions: DSLConditionBuilder.() -> Unit) {
        DSLHavingFunctionBuilder(dialect, tableOrAliasName, andJoint).builderActions()
    }

    fun or(builderActions: DSLHavingBuilder.() -> Unit) {
        val builder = DSLHavingBuilder(dialect, tableName)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLHavingBuilder.() -> Unit) {
        val builder = DSLHavingBuilder(dialect, tableName)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}