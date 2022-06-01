package io.hatis.kodbq

data class ReferenceDefinition(
    val columnDefinition: ColumnDefinition,
    val referredColumnDefinition: ColumnDefinition
)

data class ColumnDefinition(val table: Table, val name: String) {
    internal fun toColunm(dialect: SqlDialect, alias: String? = null) = Column(
        table = table.name,
        name = name,
        dialect = dialect,
        alias = alias
    )

    infix fun refernce(cd: ColumnDefinition) = ReferenceDefinition(this, cd)
}

open class Table(internal val name: String) {
    fun column(columnName: String) = ColumnDefinition(this, columnName)
    fun getTableName() = name
}