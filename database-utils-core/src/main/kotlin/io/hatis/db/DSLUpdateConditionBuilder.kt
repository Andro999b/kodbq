package io.hatis.db

class DSLUpdateConditionBuilder(mode: SqlMode) : DSLHierarchyConditionBuilder(mode, null) {
    fun or(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val builder = DSLUpdateConditionBuilder(mode)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val builder = DSLUpdateConditionBuilder(mode)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}