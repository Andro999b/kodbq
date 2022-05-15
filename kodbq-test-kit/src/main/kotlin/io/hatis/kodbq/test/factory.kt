package io.hatis.kodbq.test

import io.hatis.kodbq.SqlBuilder
import io.kotest.core.spec.style.stringSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime

typealias ExecuteAndGetFun = SqlBuilder.() -> List<Map<String, Any>>

fun selectsTestFactory(execute: ExecuteAndGetFun) = stringSpec {
    "select all" {
        val users = selectAllUsers().execute()
        users.size shouldBeGreaterThan 1
        isUserBob(users.first())
    }
    "count users" {
        val res = countUsers().execute().first()
        res["count"] shouldBe 6
    }
    "select users names" {
        val names = selectUsersNames()
            .execute()
            .map { it["name"] }
            .toList()
        val expectedNames = setOf("Mike", "Joe", "Alice", "Bob", "John", "Bob Junior")
        names.forExactly(expectedNames.size) { name ->
            should { expectedNames.contains(name) }
        }
    }
    "select user with id" {
        val userId = 1L
        val users = selectByUserId(userId).execute()
        users.size shouldBe 1
        isUserBob(users.first())
    }
    "select user with id collection" {
        val users = selectByUserIds(listOf(1, 2)).execute()
        users.size shouldBe 2
        isUserBob(users[0])
        isUserAlice(users[1])
    }
    "select with offset" {
        val users = selectUsersWithOffset(1, 2)
            .execute()
        users.size shouldBe 2
        isUserAlice(users[0])
        isUserJoe(users[1])
    }
    "select with range" {
        val users = selectUsersInRange(1, 3).execute()
        users.size shouldBe 2
        isUserAlice(users[0])
        isUserJoe(users[1])
    }
    "select users age between" {
        val users = selectUserAgeBetween(5, 10).execute()
        users.size shouldBe 2
        isUserJoe(users[0])
        isUserBobJunior(users[1])
    }
    "select users with name like" {
        val users = selectUserWithNameLike("Bob%").execute()
        users.size shouldBe 2
        isUserBob(users[0])
        isUserBobJunior(users[1])
    }
    "select deleted users" {
        val users = selectDeletedUsers().execute()
        users.size shouldBe 1
        isUserJoe(users.first())
    }
    "select users age and name" {
        val users = selectUsersNameAndAge().execute()
        val firstUser = users.first()
        val firstUserFields = firstUser.keys
        firstUserFields.size shouldBe 2
        should { firstUserFields.contains("age") }
        should { firstUserFields.contains("name") }
    }
    "select orders since" {
        val time = OffsetDateTime.now()
        val orders = selectOrderSince(time).execute()
        orders.map { it["article"] } shouldBe listOf("phone", "laptop", "charger")
    }
    "select orders until" {
        val time = OffsetDateTime.now().minusDays(1)
        val orders = selectOrderUntil(time).execute()
        orders.map { it["article"] } shouldBe listOf("pc", "lamp")
    }
    "select user orders" {
        val userId = 1L
        val orders = selectUserOrders(userId).execute()
        orders.map { it["orderid"] } shouldBe listOf(1, 2)
        orders.forEach {
            it["name"] shouldBe "Bob"
            it["userid"] shouldBe userId
        }
    }
    "select orders with user from" {
        val from = OffsetDateTime.now()
        val orders = selectUsersOrdersFrom(from).execute()
        orders.map { it["article"] } shouldBe listOf("phone", "laptop", "charger")
        orders.map { it["price"] } shouldBe listOf(300.0, 800.0, 20.0)
        orders.map { it["name"] } shouldBe listOf("Alice", "John", "John")
        orders.map { it["orderid"] } shouldBe listOf(3, 4, 5)
        orders.map { it["order_status"] } shouldBe listOf("new", "paid", "shipped")
    }
    "select users orders count" {
        val result = selectUsersOrdersCount().execute()
        result.map { it["name"] } shouldBe listOf("Bob", "Alice", "John")
        result.map { it["count"] } shouldBe listOf(2, 1, 3)
    }
    "select users avg order price" {
        val result = selectUsersAvgOrderPrice().execute()
        result.map { it["name"] } shouldBe listOf("Bob", "Alice", "John")
        result.map { it["avg_price"] } shouldBe listOf(510.0, 300.0, 280.0)
    }
    "select users sorted by age desc and created date asc" {
        val users = selectUsersSortedByAgeAndCreated().execute()
        users.map { it["name"] } shouldBe listOf("Bob", "Mike", "John", "Alice", "Joe", "Bob Junior")
    }
    "select orders sorted by created and price" {
        val orders = selectOrdersByCteatedAndPrice().execute()
        orders.map { it["article"] } shouldBe listOf("lamp", "pc", "to_rename", "charger", "phone", "laptop")
    }
    "select users with age in ranges" {
        val ranges = listOf(
            5..10,
            16..18,
            40..60
        )
        val users = selectUsersWithAgeInRanges(ranges).execute()
        users.map { it["name"] } shouldBe listOf("Bob", "Alice", "Joe", "Bob Junior")
    }
}

fun updateTestFactory(execute: ExecuteAndGetFun) = stringSpec {
    "insert orders" {
        val userId = 6L
        val orders = listOf(
            TestOrder(article = "wheat", userId = userId, price = 1.0),
            TestOrder(article = "carrot", userId = userId, price = 10.0),
            TestOrder(article = "potato", userId = userId, price = 5.0)
        )
        insertOrders(orders).execute()
        val result = selectUserOrders(userId).execute()
        result.size shouldBe orders.size
        result.map { it["orderid"] } shouldBe listOf(100, 101, 102)
    }
    "change order article" {
        val orderId = 6L
        val article = "onion"
        val price = 20.0
        var result = updateOrder(orderId, article, price).execute()
        result.size shouldBe 1
        result.first()["affectedRows"] shouldBe 1
        result = selectOrderById(orderId).execute()
        result.first()["article"] shouldBe article
        result.first()["price"] shouldBe price
    }
    "delete order" {
        val orderId = 100L
        var result = deleteOrder(orderId).execute()
        result.size shouldBe 1
        result.first()["affectedRows"] shouldBe 1
        result = selectOrderById(orderId).execute()
        result.size shouldBe 0
    }
    "delete orders created in date range" {
        val ranges = listOf(OffsetDateTime.now().minusDays(1) to OffsetDateTime.now().plusDays(1))
        val result = deleteOrdersWithDateRanges(ranges).execute()
        result.size shouldBe 1
        should { (result.first()["affectedRows"] as Number).toInt() >= 1 }
    }
}

private fun isUserBob(user: Map<String, Any?>) {
    user["id"] shouldBe 1
    user["name"] shouldBe "Bob"
    user["age"] shouldBe 52
    user["deleted"] shouldBe null
}

private fun isUserBobJunior(user: Map<String, Any?>) {
    user["id"] shouldBe 6
    user["name"] shouldBe "Bob Junior"
    user["age"] shouldBe 7
    user["deleted"] shouldBe null
}

private fun isUserAlice(user: Map<String, Any?>) {
    user["id"] shouldBe 2
    user["name"] shouldBe "Alice"
    user["age"] shouldBe 16
    user["deleted"] shouldBe null
}

private fun isUserJoe(user: Map<String, Any?>) {
    user["id"] shouldBe 3
    user["name"] shouldBe "Joe"
    user["age"] shouldBe 8
    user["deleted"] shouldNotBe null
}

