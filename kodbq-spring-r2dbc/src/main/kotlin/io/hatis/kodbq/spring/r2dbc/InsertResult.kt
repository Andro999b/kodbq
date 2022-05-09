package io.hatis.kodbq.spring.r2dbc

data class InsertResult(
    val affectedRows: Int,
    val generatedKeys: List<Map<String, Any>>
)
