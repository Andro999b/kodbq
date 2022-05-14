package io.hatis.kodbq.test

import io.hatis.kodbq.SqlBuilder
import io.kotest.matchers.shouldBe


fun SqlBuilder.expectSqlAndParams(sql: String, params: List<Any?> = emptyList()) {
    val (actualSql, actualParams) = buildSqlAndParams()

    actualSql shouldBe sql
    actualParams shouldBe params
}