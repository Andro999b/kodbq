package io.hatis.db

class UpdateBuilder(
    val tableName: String,
    val columns: Map<String, Any?>,
    val where: WherePart?
)