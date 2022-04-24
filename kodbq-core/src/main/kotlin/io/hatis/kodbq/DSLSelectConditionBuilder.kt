package io.hatis.kodbq

class DSLSelectConditionBuilder(mode: SqlDialect, private val tableName: String? = null) :
    DSLHierarchyConditionBuilder(mode, tableName) {
    fun table(tableOrAliasName: String, builderActions: DSLConditionBuilder.() -> Unit) {
        DSLConditionBuilder(mode, tableOrAliasName, andJoint).builderActions()
    }

    fun or(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(mode, tableName)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(mode, tableName)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}