package io.hatis.kodbq

enum class SqlDialect(val escape: (String) -> String) {
    PG({ "\"$it\"" }),
    MY_SQL({ "`$it`" }),
    MS_SQL({ "[$it]" })
}