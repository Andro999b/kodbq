package io.hatis.kodbq.test

import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.kodbqDialect
import io.kotest.core.spec.style.StringSpec
import java.time.OffsetDateTime

class TestUpdate: StringSpec({
    beforeTest { kodbqDialect = SqlDialect.PG }
    "insert orders with pg returning" {
        val orders = listOf(
            TestOrder(article = "carrot", userId = 1, price = 10.0),
            TestOrder(article = "potato", userId = 2, price = 5.0),
            TestOrder(article = "wheat", userId = 1, price = 1.0)
        )
        insertOrders(orders)
            .expectSqlAndParams(
                "insert into \"orders\"(\"user_id\",\"article\",\"price\",\"created\") values(?,?,?,now()) returning \"id\"",
                orders.map { listOf(it.userId, it.article, it.price) }
            )
    }
    "insert orders with mssql output" {
        kodbqDialect = SqlDialect.MS_SQL
        val orders = listOf(
            TestOrder(article = "carrot", userId = 1, price = 10.0),
            TestOrder(article = "potato", userId = 2, price = 5.0),
            TestOrder(article = "wheat", userId = 1, price = 1.0)
        )
        insertOrders(orders)
            .expectSqlAndParams(
                "insert into [orders]([user_id],[article],[price],[created]) output inserted.[id] values(?,?,?,getdate())",
                orders.map { listOf(it.userId, it.article, it.price) }
            )
    }
    "insert orders" {
        kodbqDialect = SqlDialect.MY_SQL
        val orders = listOf(
            TestOrder(article = "carrot", userId = 1, price = 10.0),
            TestOrder(article = "potato", userId = 2, price = 5.0),
            TestOrder(article = "wheat", userId = 1, price = 1.0)
        )
        insertOrders(orders)
            .expectSqlAndParams(
                "insert into `orders`(`user_id`,`article`,`price`,`created`) values(?,?,?,now())",
                orders.map { listOf(it.userId, it.article, it.price) }
            )
    }
    "change order article" {
        val orderId = 1L
        val article = "onion"
        val price = 15.0
        updateOrder(orderId, article, price)
            .expectSqlAndParams(
                "update \"orders\" set \"article\"=?,\"price\"=? where \"orders\".\"id\"=?",
                listOf(article, price, orderId)
            )
    }
    "delete order" {
        val orderId = 1L
        deleteOrder(orderId)
            .expectSqlAndParams(
                "delete from \"orders\" where \"orders\".\"id\"=?",
                listOf(orderId)
            )
    }
    "delete orders created in future date ranges" {
        val ranges = listOf(
            OffsetDateTime.now().plusDays(1) to OffsetDateTime.now().plusDays(2),
            OffsetDateTime.now().plusDays(10) to OffsetDateTime.now().plusDays(30),
        )
        deleteOrdersWithDateRanges(ranges)
            .expectSqlAndParams(
                "delete from \"orders\" where (\"orders\".\"created\">? and \"orders\".\"created\"<?) or (\"orders\".\"created\">? and \"orders\".\"created\"<?)",
                ranges.flatMap { listOf(it.first, it.second) }
            )
    }
})