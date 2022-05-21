package io.hatis.kodbq

class DSLUnionSelectBuilder(tableName: String, dialect: SqlDialect): DSLSelectBuilder(tableName, dialect) {
    private val unions = mutableListOf<Pair<DSLSelectBuilder, Boolean>>()

    fun union(all: Boolean = false, builderActions: DSLSelectBuilder.() -> Unit) {
        union(tableName, all, builderActions)
    }

    fun union(tableName: String, all: Boolean = false, builderActions: DSLSelectBuilder.() -> Unit) {
        val dslSelectBuilder = DSLSelectBuilder(tableName, dialect)
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