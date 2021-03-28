package io.hatis.db.quarkus

import io.hatis.db.*
import io.hatis.db.quarkus.CoroutineTxActions.Companion.inTransaction
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Row

abstract class CrudRepository<T> {
    protected abstract val mapper: (rs: Row) -> T
    protected abstract val tableName: String

    open suspend fun insert(actions: DSLMutationColumnsBuilder.() -> Unit) = inTransaction { tx ->
        sqlInsert(tableName) {
            values { actions() }
        }
            .execute(tx)
            .awaitSuspending()
    }

    open suspend fun insertAll(values: Collection<T>, actions: DSLMutationColumnsBuilder.(T) -> Unit) =
        inTransaction { tx ->
            sqlInsert(tableName) {
                values.forEach {
                    values { actions(it) }
                }
                generatedKeys("id")
            }
                .execute(tx)
                .awaitSuspending()
        }

    open suspend fun insertAndGetId(actions: DSLMutationColumnsBuilder.() -> Unit): Long = inTransaction { tx ->
        sqlInsert(tableName) {
            values { actions() }
            generatedKeys("id")
        }
            .execute(tx)
            .awaitSuspending()
            .first()
            .getLong("id")
    }

    open suspend fun update(actions: DSLUpdateBuilder.() -> Unit) = inTransaction { tx ->
        sqlUpdate(tableName) { actions() }
            .execute(tx)
    }

    open suspend fun delete(actions: DSLUpdateConditionBuilder.() -> Unit) = inTransaction { tx ->
        sqlDelete(tableName) { where { actions() } }
            .execute(tx)
    }

    open suspend fun exist(actions: DSLSelectConditionBuilder.() -> Unit): Boolean = inTransaction { tx ->
        sqlSelect(tableName) {
            aggregation { count("count") }
            where { actions() }
        }
            .execute(tx)
            .awaitSuspending()
            .first().getBoolean("count")
    }

    open suspend fun select(actions: DSLSelectBuilder.() -> Unit): Collection<T> = inTransaction { tx ->
        val rows = sqlSelect(tableName) { actions() }
            .execute(tx)
            .awaitSuspending()

        rows.map(mapper)
    }

    open suspend fun selectOne(actions: DSLSelectBuilder.() -> Unit): T? = inTransaction { tx ->
        sqlSelect(tableName) {
            actions()
            limit(1)
        }
            .execute(tx)
            .awaitSuspending()
            .firstOrNull()
            ?.let(mapper)
    }

    open suspend fun selectWhere(actions: DSLSelectConditionBuilder.() -> Unit): Collection<T> = inTransaction { tx ->
        val rows = sqlSelect(tableName) { where { actions() } }
            .execute(tx)
            .awaitSuspending()

        rows.map(mapper)
    }

    open suspend fun selectOneWhere(actions: DSLSelectConditionBuilder.() -> Unit): T? = inTransaction { tx ->
        sqlSelect(tableName) {
            where { actions() }
            limit(1)
        }
            .execute(tx)
            .awaitSuspending()
            .firstOrNull()
            ?.let(mapper)
    }

    open suspend fun selectById(id: Long): T? = inTransaction { tx ->
        sqlSelect(tableName) { where { id(id) } }
            .execute(tx)
            .awaitSuspending()
            .firstOrNull()
            ?.let(mapper)
    }

    open suspend fun selectById(ids: Collection<Long>): Collection<T> =
        if (ids.isEmpty())
            emptyList()
        else
            inTransaction { tx ->
                sqlSelect(tableName) { where { column("id", ids) } }
                    .execute(tx)
                    .awaitSuspending()
                    .map(mapper)
            }
}