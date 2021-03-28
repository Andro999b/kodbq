package io.hatis.db.quarkus

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.SqlClient
import io.vertx.mutiny.sqlclient.SqlClientHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.lang.IllegalStateException
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class CoroutineTx(val client: SqlClient) : AbstractCoroutineContextElement(CoroutineTx) {
    companion object Key : CoroutineContext.Key<CoroutineTx>

    override fun toString(): String = "CoroutineTx($client)"
}

class CoroutineTxActions(
    private val pool: Pool
) {

    fun <T> withTxUni(body: suspend () -> T): Uni<T> = SqlClientHelper.inTransactionUni(pool) { tx ->
        GlobalScope.async(Dispatchers.Unconfined + CoroutineTx(tx)) { body() }.asUni()
    }

    companion object {
        suspend fun <T> inTransaction(body: suspend (client: SqlClient) -> T): T {
            val client = coroutineContext[CoroutineTx]?.client ?: throw IllegalStateException("No transaction found")
            return body(client)
        }

    }
}