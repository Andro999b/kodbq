package io.hatis.kodbq.jdbc

import io.hatis.kodbq.SqlDialect

class JdbcTestPostgres: JdbcTest(
    "jdbc:tc:postgresql:14.2:///test?TC_INITSCRIPT=postgres.sql",
    SqlDialect.PG
)