package io.hatis.kodbq

class ReturnColumn(name: String, dialect: SqlDialect, table: String? = null, val alias: String? = null) :
    Column(name, dialect, table)