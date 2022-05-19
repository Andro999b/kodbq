package io.hatis.kodbq

var kodbqDialect = SqlDialect.SQL92

fun sqlInsert(
    tableName: String,
    dialect: SqlDialect = kodbqDialect,
    builderActions: DSLInsertBuilder.() -> Unit
): InsertBuilder {
    val dslInsertBuilder = DSLInsertBuilder(tableName, dialect)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createInsertBuilder()
}

fun sqlUpdate(
    tableName: String,
    dialect: SqlDialect = kodbqDialect,
    builderActions: DSLUpdateBuilder.() -> Unit
): UpdateBuilder {
    val dslInsertBuilder = DSLUpdateBuilder(dialect)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createUpdateBuilder(tableName)
}

fun sqlSelect(
    tableName: String,
    dialect: SqlDialect = kodbqDialect,
    builderActions: (DSLSelectBuilder.() -> Unit)? = null
): SelectBuilder {
    val dslInsertBuilder = DSLSelectBuilder(tableName, dialect)
    if(builderActions != null) dslInsertBuilder.builderActions()
    return dslInsertBuilder.createSelectBuilder()
}

fun sqlDelete(
    tableName: String,
    dialect: SqlDialect = kodbqDialect,
    builderActions: DSLDeleteBuilder.() -> Unit
): DeleteBuilder {
    val dslInsertBuilder = DSLDeleteBuilder(tableName, dialect)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createDeleteBuilder()
}

fun sql(
    dialect: SqlDialect = kodbqDialect,
    generatorFun: NativeSql.Generator.() -> String
) = QueryBuilder(NativeSql(dialect, generatorFun))


