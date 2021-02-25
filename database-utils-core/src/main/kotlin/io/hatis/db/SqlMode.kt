package io.hatis.db

enum class SqlMode(val escape: (String) -> String) {
    PG({ "\"$it\"" }),
    MY_SQL({ "`$it`" }),
    MS_SQL({ "[$it]" })
}