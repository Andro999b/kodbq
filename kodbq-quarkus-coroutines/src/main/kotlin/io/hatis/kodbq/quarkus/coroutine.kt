package io.hatis.kodbq.quarkus

import io.hatis.kodbq.InsertBuilder
import io.hatis.kodbq.QueryBuilder
import io.hatis.kodbq.SelectBuilder
import io.hatis.kodbq.SqlBuilder
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.SqlClient

suspend fun SqlBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun InsertBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun <T> SelectBuilder.awaitFirst(client: SqlClient, mapper: (Row) -> T): T? = await(client).firstOrNull()?.let(mapper)
suspend fun <T> SelectBuilder.awaitAll(client: SqlClient, mapper: (Row) -> T): Collection<T>  = await(client).map(mapper)
suspend fun <T> QueryBuilder.awaitFirst(client: SqlClient, mapper: (Row) -> T): T? = await(client).firstOrNull()?.let(mapper)
suspend fun <T> QueryBuilder.awaitAll(client: SqlClient, mapper: (Row) -> T): Collection<T>  = await(client).map(mapper)