package io.hatis.db

open class DSLHierarchyConditionBuilder(mode: SqlMode, tableName: String? = null) :
    DSLConditionBuilder(mode, tableName) {

    protected val orJoint: Or = Or()

    internal fun createWhereCondition(): WherePart =
        if (orJoint.parts.isEmpty()) {
            andJoint
        } else {
            orJoint.parts.add(0, andJoint)
            orJoint
        }
}