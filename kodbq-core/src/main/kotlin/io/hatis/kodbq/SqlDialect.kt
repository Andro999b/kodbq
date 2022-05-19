package io.hatis.kodbq

enum class SqlDialect(val escape: (String) -> String) {
    SQL92({ "\"$it\"" }),
    PG({ "\"$it\"" }),
    MY_SQL({ "`$it`" }),
    MS_SQL({ "[$it]" })
}