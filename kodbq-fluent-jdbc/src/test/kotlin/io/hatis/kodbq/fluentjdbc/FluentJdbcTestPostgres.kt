package io.hatis.kodbq.fluentjdbc

import io.hatis.kodbq.SqlDialect

class FluentJdbcTestPostgres: FluentJdbcTest(
    "jdbc:tc:postgresql:14.2:///test?TC_INITSCRIPT=file:../sql/postgres.sql",
    SqlDialect.PG
)