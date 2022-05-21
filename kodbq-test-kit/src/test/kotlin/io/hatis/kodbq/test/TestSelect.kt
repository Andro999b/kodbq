package io.hatis.kodbq.test

import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.kodbqDialect
import io.kotest.core.spec.style.StringSpec
import java.time.OffsetDateTime

class TestSelect : StringSpec({
    beforeTest { kodbqDialect = SqlDialect.PG }
    "select all" {
        selectAllUsers()
            .expectSqlAndParams("select * from \"users\"")
    }
    "count users" {
        countUsers()
            .expectSqlAndParams("select count(*) as count from \"users\"")
    }
    "select users names" {
        selectUsersNames()
            .expectSqlAndParams("select distinct \"users\".\"name\" from \"users\"")
    }
    "select user with id" {
        val userId = 1L
        selectByUserId(userId)
            .expectSqlAndParams("select * from \"users\" where \"users\".\"id\"=?", listOf(userId))
    }
    "select user with id collection" {
        val userIds: List<Long> = listOf(1, 2, 3, 4)
        selectByUserIds(userIds)
            .expectSqlAndParams("select * from \"users\" where \"users\".\"id\" in(?,?,?,?)", userIds)
    }
    "select with offset" {
        selectUsersWithOffset(100, 10)
            .expectSqlAndParams("select * from \"users\" order by \"users\".\"id\" limit 10 offset 100")
    }
    "select with offset for MS_SQL" {
        kodbqDialect = SqlDialect.MS_SQL
        selectUsersWithOffset(100, 0)
            .expectSqlAndParams("select * from [users] order by [users].[id] offset 100 rows")
    }
    "select with offset and limit for MS_SQL" {
        kodbqDialect = SqlDialect.MS_SQL
        selectUsersWithOffset(100, 10)
            .expectSqlAndParams("select * from [users] order by [users].[id] offset 100 rows fetch next 10 rows only")
    }
    "select with range" {
        selectUsersInRange(100, 110)
            .expectSqlAndParams("select * from \"users\" order by \"users\".\"id\" limit 10 offset 100")
    }
    "select users age between" {
        selectUserAgeBetween(18, 25)
            .expectSqlAndParams(
                "select * from \"users\" where \"users\".\"age\">=? and \"users\".\"age\"<=?",
                listOf(18, 25)
            )
    }
    "select users with name like" {
        selectUserWithNameLike("mike")
            .expectSqlAndParams(
                "select * from \"users\" where \"users\".\"name\" like ?",
                listOf("mike")
            )
    }
    "select deleted users" {
        selectDeletedUsers()
            .expectSqlAndParams("select * from \"users\" where \"users\".\"deleted\" is not null")
    }
    "select users age and name" {
        selectUsersNameAndAge()
            .expectSqlAndParams("select \"users\".\"name\",\"users\".\"age\" as userAge from \"users\"")
    }
    "select orders since" {
        val time = OffsetDateTime.now()
        selectOrderSince(time)
            .expectSqlAndParams("select * from \"orders\" where \"orders\".\"created\">?", listOf(time))
    }
    "select orders until" {
        val time = OffsetDateTime.now()
        selectOrderUntil(time)
            .expectSqlAndParams("select * from \"orders\" where \"orders\".\"created\"<?", listOf(time))
    }
    "select user orders" {
        val userId = 1L
        selectUserOrders(userId)
            .expectSqlAndParams(
                "select \"orders\".\"id\" as orderid,\"users\".\"name\",\"users\".\"id\" as userid " +
                        "from \"orders\" join \"users\" on \"users\".\"id\"=\"orders\".\"user_id\" " +
                        "where \"orders\".\"user_id\"=?",
                listOf(userId)
            )
    }
    "select orders with user from" {
        val from = OffsetDateTime.now()
        selectUsersOrdersFrom(from)
            .expectSqlAndParams(
                "select " +
                        "\"orders\".\"id\" as orderid," +
                        "\"orders\".\"article\"," +
                        "\"orders\".\"price\"," +
                        "\"users\".\"name\"," +
                        "\"users\".\"id\" as userid," +
                        "\"order_status\".\"status\" as order_status " +
                        "from \"orders\" " +
                        "join \"users\" on \"users\".\"id\"=\"orders\".\"user_id\" " +
                        "join \"order_status\" on \"order_status\".\"order_id\"=\"orders\".\"id\" " +
                        "where \"orders\".\"created\">=? and \"users\".\"deleted\" is null",
                listOf(from)
            )
    }
    "select users id with order min price" {
        val minPrice = 10
        selectUsersWithOrderMinPrice(minPrice)
            .expectSqlAndParams(
                "select \"orders\".\"user_id\" from \"orders\" group by \"orders\".\"user_id\" having max(\"orders\".\"price\")>=?",
                listOf(minPrice)
            )
    }
    "select users orders count" {
        selectUsersOrdersCount()
            .expectSqlAndParams(
                "select \"users\".\"name\",\"users\".\"id\",count(*) as count " +
                        "from \"users\" right join \"orders\" on \"orders\".\"user_id\"=\"users\".\"id\" " +
                        "group by \"users\".\"id\",\"users\".\"name\" " +
                        "order by \"users\".\"id\""
            )
    }
    "select users avg order price" {
        selectUsersAvgOrderPrice()
            .expectSqlAndParams("select \"users\".\"name\",\"users\".\"id\",avg(\"orders\".\"price\") as avg_price " +
                    "from \"users\" " +
                    "right join \"orders\" on \"orders\".\"user_id\"=\"users\".\"id\" " +
                    "group by \"users\".\"id\",\"users\".\"name\" " +
                    "order by \"users\".\"id\""
            )
    }
    "select users sorted by age desc and created date asc" {
        selectUsersSortedByAgeAndCreated()
            .expectSqlAndParams("select * from \"users\" order by \"users\".\"age\" desc,\"users\".\"created\"")
    }
    "select orders sorted by created and price" {
        selectOrdersByCreatedAndPrice()
            .expectSqlAndParams("select * from \"orders\" order by \"orders\".\"created\",\"orders\".\"price\"")
    }
    "select users with age in ranges" {
        val ranges = listOf(
            5..10,
            16..18,
            40..60
        )
        selectUsersWithAgeInRanges(ranges)
            .expectSqlAndParams(
                "select * from \"users\" where " +
                        "(\"users\".\"age\">=? and \"users\".\"age\"<=?) or " +
                        "(\"users\".\"age\">=? and \"users\".\"age\"<=?) or " +
                        "(\"users\".\"age\">=? and \"users\".\"age\"<=?)",
                ranges.flatMap { listOf(it.first, it.last) }
            )
    }
    "select user ids of laptop and charger orders" {
        selectUserIdsOfLaptopAndChargerOrders()
            .expectSqlAndParams(
                "select * from \"orders\" where \"orders\".\"article\"=?\n" +
                        "union all\n" +
                        "select * from \"orders\" where \"orders\".\"article\"=?",
                listOf("laptop", "charger")
            )
    }
})
