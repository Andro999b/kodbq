package io.hatis.kodbq

open class DSLHierarchyConditionBuilder(mode: SqlDialect, tableName: String?): DSLConditionBuilder(mode, tableName) {
    protected val orJoint: Or = Or()

    internal fun createWhereCondition(): WherePart =
        if (orJoint.parts.isEmpty()) {
            andJoint
        } else {
            orJoint.parts.add(0, andJoint)
            orJoint
        }
}