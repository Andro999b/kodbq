package io.hatis.db

class InsertBuilder(
    val tableName: String,
    val columns: Map<String, Any?>,
    val generatedKeys: Set<String> = emptySet()
)