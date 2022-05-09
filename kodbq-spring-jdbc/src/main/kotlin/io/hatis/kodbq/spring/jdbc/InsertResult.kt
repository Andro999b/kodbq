package io.hatis.kodbq.spring.jdbc

data class InsertResult(
    val affectedRows: Int,
    val generatedKeys: List<Map<String, Any>>
)
