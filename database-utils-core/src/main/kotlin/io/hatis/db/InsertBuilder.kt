package io.hatis.utils.db.io.hatis

class InsertBuilder(
    val tableName: String,
    val columns: Map<String, Any?>,
    val generatedKeys: Set<String> = emptySet()
)