package io.hatis.db.quarkus

import io.hatis.db.DeleteBuilder
import io.hatis.db.InsertBuilder
import io.hatis.db.SelectBuilder
import io.hatis.db.UpdateBuilder
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.sqlclient.SqlClient

suspend fun UpdateBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun InsertBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun SelectBuilder.await(client: SqlClient) = execute(client).awaitSuspending()
suspend fun DeleteBuilder.await(client: SqlClient) = execute(client).awaitSuspending()