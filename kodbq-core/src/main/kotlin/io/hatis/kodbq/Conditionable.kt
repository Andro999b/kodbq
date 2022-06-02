package io.hatis.kodbq

interface Conditionable {
    fun toConditionName(dialect: SqlDialect): Named
}