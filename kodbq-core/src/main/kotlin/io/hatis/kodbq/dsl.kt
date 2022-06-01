package io.hatis.kodbq

var kodbqDialect = SqlDialect.SQL92

fun sqlInsert(
    table: Table,
    dialect: SqlDialect = kodbqDialect,
    builderActions: DSLInsertBuilder.() -> Unit
): InsertBuilder {
    val dslInsertBuilder = DSLInsertBuilder(table, dialect)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createInsertBuilder()
}

fun sqlUpdate(
    table: Table,
    dialect: SqlDialect = kodbqDialect,
    builderActions: DSLUpdateBuilder.() -> Unit
): UpdateBuilder {
    val dslInsertBuilder = DSLUpdateBuilder(table, dialect)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createUpdateBuilder()
}

fun sqlSelect(
    table: Table,
    dialect: SqlDialect = kodbqDialect,
    builderActions: (DSLUnionSelectBuilder.() -> Unit)? = null
): SelectBuilder {
    val dslInsertBuilder = DSLUnionSelectBuilder(table, dialect)
    if(builderActions != null) dslInsertBuilder.builderActions()
    return dslInsertBuilder.createSelectBuilder()
}

fun sqlDelete(
    table: Table,
    dialect: SqlDialect = kodbqDialect,
    builderActions: (DSLDeleteBuilder.() -> Unit)? = null
): DeleteBuilder {
    val dslInsertBuilder = DSLDeleteBuilder(table, dialect)
    if(builderActions != null) dslInsertBuilder.builderActions()
    return dslInsertBuilder.createDeleteBuilder()
}

fun sql(
    dialect: SqlDialect = kodbqDialect,
    generatorFun: NativeSql.Generator.() -> String
) = QueryBuilder(NativeSql(dialect, generatorFun))