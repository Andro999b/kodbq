package io.hatis.utils.db.io.hatis

class UpdateBuilder(
    val tableName: String,
    val columns: Map<String, Any?>,
    val where: WherePart?
)