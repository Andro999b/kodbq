package io.hatis.kodbq

class DSLSelectConditionBuilder(dialect: SqlDialect, tableName: String? = null) :
    DSLConditionBuilder(dialect, tableName, And()) {
    fun table(tableOrAliasName: String, builderActions: DSLConditionBuilder.() -> Unit) {
        DSLConditionBuilder(dialect, tableOrAliasName, andJoint).builderActions()
    }

    fun or(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(dialect, tableName)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(dialect, tableName)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}