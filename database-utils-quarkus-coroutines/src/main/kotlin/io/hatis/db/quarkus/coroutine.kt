package io.hatis.db.quarkus

import io.hatis.db.DeleteBuilder
import io.hatis.db.InsertBuilder
import io.hatis.db.SelectBuilder
import io.hatis.db.UpdateBuilder
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.SqlClient

suspend fun UpdateBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun InsertBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun SelectBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun <T> SelectBuilder.awaitFirst(client: SqlClient, mapper: (Row) -> T): T? = await(client).firstOrNull()?.let(mapper)
suspend fun <T> SelectBuilder.awaitAll(client: SqlClient, mapper: (Row) -> T): Collection<T>  = await(client).map(mapper)
suspend fun DeleteBuilder.await(client: SqlClient) = execute(client).awaitSuspending()