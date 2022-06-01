package io.hatis.kodbq

class DSLSortBuilder(
    private val dialect: SqlDialect,
    internal val sortColumns: MutableSet<SelectBuilder.Sort> = mutableSetOf()
) {
    fun count(asc: Boolean = true) {
        sortColumns += SelectBuilder.Sort(SimpleFunction("count", Column("*", dialect)), asc)
    }
    fun count(c: ColumnDefinition, asc: Boolean = true) = function("count", c, asc)
    fun max(c: ColumnDefinition, asc: Boolean = true) = function("max", c, asc)
    fun min(c: ColumnDefinition, asc: Boolean = true) = function("min", c, asc)
    fun avg(c: ColumnDefinition, asc: Boolean = true) = function("avg", c, asc)
    fun sum(c: ColumnDefinition, asc: Boolean = true) = function("sum", c, asc)

    fun function(function: String, c: ColumnDefinition, asc: Boolean) {
        sortColumns += SelectBuilder.Sort(SimpleFunction(function, c.toColunm(dialect)), asc)
    }

    fun asc(vararg cds: ColumnDefinition) {
        cds.forEach {
            sortColumns += SelectBuilder.Sort(it.toColunm(dialect))
        }
    }

    fun desc(vararg cds: ColumnDefinition) {
        cds.forEach {
            sortColumns += SelectBuilder.Sort(it.toColunm(dialect), false)
        }
    }
}