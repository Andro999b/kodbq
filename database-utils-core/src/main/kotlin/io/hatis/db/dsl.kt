package io.hatis.db

fun sqlInsert(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLInsertBuilder.() -> Unit
): InsertBuilder {
    val dslInsertBuilder = DSLInsertBuilder(tableName, mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createInsertBuilder()
}

fun sqlUpdate(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLUpdateBuilder.() -> Unit
): UpdateBuilder {
    val dslInsertBuilder = DSLUpdateBuilder(mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createUpdateBuilder(tableName)
}

fun sqlSelect(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: (DSLSelectBuilder.() -> Unit)? = null
): SelectBuilder {
    val dslInsertBuilder = DSLSelectBuilder(tableName, mode)
    if(builderActions != null) dslInsertBuilder.builderActions()
    return dslInsertBuilder.createSelectBuilder()
}

fun sqlDelete(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLDeleteBuilder.() -> Unit
): DeleteBuilder {
    val dslInsertBuilder = DSLDeleteBuilder(tableName, mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createDeleteBuilder()
}

