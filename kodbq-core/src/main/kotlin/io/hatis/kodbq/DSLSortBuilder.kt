package io.hatis.kodbq

open class DSLSortBuilder(
    protected val dialect: SqlDialect,
    protected val tableName: String,
    internal val sortColumns: MutableSet<SelectBuilder.Sort> = mutableSetOf()
) {
    fun count(asc: Boolean = true) = count("*", asc)
    fun count(columnName: String, asc: Boolean = true) = function("count", columnName, asc)
    fun max(columnName: String, asc: Boolean = true) = function("max", columnName, asc)
    fun min(columnName: String, asc: Boolean = true) = function("min", columnName, asc)
    fun avg(columnName: String, asc: Boolean = true) = function("avg", columnName, asc)
    fun sum(columnName: String, asc: Boolean = true) = function("sum", columnName, asc)

    fun function(function: String, columnName: String, asc: Boolean) {
        sortColumns += SelectBuilder.Sort(SimpleFunction(function, Column(columnName, dialect, tableName)), asc)
    }

    fun asc(vararg columnName: String) {
        columnName.forEach {
            sortColumns += SelectBuilder.Sort(Column(it, dialect, tableName))
        }
    }

    fun desc(vararg columnName: String) {
        columnName.forEach {
            sortColumns += SelectBuilder.Sort(Column(it, dialect, tableName), false)
        }
    }
}