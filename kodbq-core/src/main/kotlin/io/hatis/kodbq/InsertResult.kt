package io.hatis.kodbq

data class InsertResult(
    val affectedRows: Int,
    val generatedKeys: List<Map<String, Any>>
)
