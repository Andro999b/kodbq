package io.hatis.db.quarkus

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asMulti
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.SqlClient
import io.vertx.mutiny.sqlclient.SqlClientHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.lang.IllegalStateException
import java.time.Duration
import kotlin.coroutines.*

class CoroutineTx(val client: SqlClient) : AbstractCoroutineContextElement(CoroutineTx) {
    companion object Key : CoroutineContext.Key<CoroutineTx>

    override fun toString(): String = "CoroutineTx($client)"
}

class CoroutineTxActions(
    private val pool: Pool
) {

    fun <T> withTxUni(body: suspend () -> T): Uni<T> = SqlClientHelper.inTransactionUni(pool) { tx ->
        CoroutineScope(context = Dispatchers.Unconfined + CoroutineTx(tx)).async { body() }.asUni()
    }

    suspend fun <T> withTx(body: suspend () -> T): T = suspendCoroutine { c ->
        withTxUni(body)
            .subscribe()
            .with({ c.resume(it) }, { c.resumeWithException(it) })
    }

    fun <T> blockingTx(
        timeout: Duration = Duration.ofSeconds(10),
        body: suspend () -> T
    ): T = withTxUni(body)
        .await()
        .atMost(timeout)

    companion object {
        suspend fun <T> inTransaction(body: suspend (client: SqlClient) -> T): T {
            val client = coroutineContext[CoroutineTx]?.client ?: throw IllegalStateException("No transaction found")
            return body(client)
        }

    }
}