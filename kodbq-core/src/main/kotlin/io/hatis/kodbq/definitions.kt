package io.hatis.kodbq

data class ReferenceDefinition(
    val columnDefinition: ColumnDefinition,
    val referredColumnDefinition: ColumnDefinition
)

data class ColumnDefinition(val table: Table, val name: String): Conditionable {
    internal fun toColumn(dialect: SqlDialect) = Column(
        table = table.name,
        name = name,
        dialect = dialect,
    )

    internal fun toReturnColumn(dialect: SqlDialect, alias: String? = null) = ReturnColumn(
        table = table.name,
        name = name,
        dialect = dialect,
        alias = alias
    )

    infix fun refernce(cd: ColumnDefinition) = ReferenceDefinition(this, cd)
    override fun toConditionName(dialect: SqlDialect) = toColumn(dialect)
}

open class Table(internal val name: String) {
    fun column(columnName: String) = ColumnDefinition(this, columnName)
    fun getTableName() = name
}