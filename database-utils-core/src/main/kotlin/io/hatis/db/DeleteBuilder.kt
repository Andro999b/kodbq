package io.hatis.db

import io.hatis.utils.db.io.hatis.WherePart

class DeleteBuilder(
    val tableName: String,
    val where: WherePart
)
