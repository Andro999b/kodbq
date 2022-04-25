package io.hatis.kodbq.test

import java.time.OffsetDateTime

data class TestUser(
    val id: Long,
    val name: String,
    val age: Int,
    val deleted: OffsetDateTime?
)