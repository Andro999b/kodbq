package io.hatis.kodbq.test

import java.time.OffsetDateTime

data class TestOrder(
    val id: Long? = null,
    val article: String,
    val created: OffsetDateTime? = null,
    val userId: Long,
    val price: Double
)