package io.hatis.db.quarkus

import io.hatis.db.*
import io.hatis.db.quarkus.CoroutineTxActions.Companion.inTransaction
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Row

abstract class CrudRepository<T> {
    protected abstract val mapper: (rs: Row) -> T
    protected abstract val tableName: String

    protected open suspend fun insert(actions: DSLMutationColumnsBuilder.() -> Unit) = inTransaction { tx ->
        sqlInsert(tableName) {
            values { actions() }
        }
            .await(tx)
    }

    protected open suspend fun insertAll(values: Collection<T>, actions: DSLMutationColumnsBuilder.(T) -> Unit) =
        inTransaction { tx ->
            if(values.isNotEmpty()) {
                sqlInsert(tableName) {
                    values.forEach { values { actions(it) } }
                }
                    .await(tx)
            }
        }

    protected open suspend fun insertAllAndGetIds(values: Collection<T>, actions: DSLMutationColumnsBuilder.(T) -> Unit) =
        inTransaction { tx ->
            if(values.isNotEmpty()) {
                sqlInsert(tableName) {
                    values.forEach { values { actions(it) } }
                    generatedKeys("id")
                }
                    .await(tx)
            }
        }

    protected open suspend fun insertAndGetId(actions: DSLMutationColumnsBuilder.() -> Unit): Long = inTransaction { tx ->
        sqlInsert(tableName) {
            values { actions() }
            generatedKeys("id")
        }
            .await(tx)
            .first()
            .getLong("id")
    }

    protected open suspend fun update(actions: DSLUpdateBuilder.() -> Unit) = inTransaction { tx ->
        sqlUpdate(tableName) { actions() }
            .await(tx)
    }

    protected open suspend fun delete(actions: DSLUpdateConditionBuilder.() -> Unit) = inTransaction { tx ->
        sqlDelete(tableName) { where { actions() } }
            .await(tx)
    }

    protected open suspend fun exist(actions: DSLSelectConditionBuilder.() -> Unit): Boolean = count(actions) > 0

    protected open suspend fun count(actions: DSLSelectConditionBuilder.() -> Unit): Int = inTransaction { tx ->
        sqlSelect(tableName) {
            aggregation { count("count") }
            where { actions() }
        }
            .await(tx)
            .first()
            .getInteger("count")
    }

    protected open suspend fun select(actions: DSLSelectBuilder.() -> Unit): Collection<T> = inTransaction { tx ->
        sqlSelect(tableName) { actions() }
            .awaitAll(tx, mapper)
    }

    protected open suspend fun selectOne(actions: DSLSelectBuilder.() -> Unit): T? = inTransaction { tx ->
        sqlSelect(tableName) {
            actions()
            limit(1)
        }
            .awaitFirst(tx, mapper)
    }

    protected open suspend fun selectWhere(actions: DSLSelectConditionBuilder.() -> Unit): Collection<T> = inTransaction { tx ->
        sqlSelect(tableName) { where { actions() } }
            .awaitAll(tx, mapper)
    }

    protected open suspend fun selectOneWhere(actions: DSLSelectConditionBuilder.() -> Unit): T? = inTransaction { tx ->
        sqlSelect(tableName) {
            where { actions() }
            limit(1)
        }
            .awaitFirst(tx, mapper)
    }

    protected open suspend fun selectById(id: Long): T? = inTransaction { tx ->
        sqlSelect(tableName) { where { id(id) } }
            .awaitFirst(tx, mapper)
    }

    protected open suspend fun selectById(ids: Collection<Long>): Collection<T> =
        if (ids.isEmpty())
            emptyList()
        else
            inTransaction { tx ->
                sqlSelect(tableName) { where { column("id", ids) } }
                    .awaitAll(tx, mapper)
            }
}