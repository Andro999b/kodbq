package io.hatis.kodbq

class DSLUnionSelectBuilder(table: Table, dialect: SqlDialect): DSLSelectBuilder(table, dialect) {
    private val unions = mutableListOf<Pair<DSLSelectBuilder, Boolean>>()

    fun union(all: Boolean = false, builderActions: DSLSelectBuilder.() -> Unit) {
        union(table, all, builderActions)
    }

    fun union(table: Table, all: Boolean = false, builderActions: DSLSelectBuilder.() -> Unit) {
        val dslSelectBuilder = DSLSelectBuilder(table, dialect)
        dslSelectBuilder.builderActions()
        unions += dslSelectBuilder to all
    }

    internal fun createSelectBuilder(): SelectBuilder {
        val selects = mutableListOf<SelectBuilder.Select>()
        selects += createSelect(false)
        if(unions.isNotEmpty())
            selects += unions.map { it.first.createSelect(it.second) }
        return SelectBuilder(selects, dialect)
    }
}