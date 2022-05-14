package io.hatis.kodbq.vertx.jdbc

import io.hatis.kodbq.SqlDialect

class VertxJDBCTestPostgres: VertxJDBCTest(
    "jdbc:tc:postgresql:14.2:///test?TC_INITSCRIPT=file:../sql/postgres.sql",
    SqlDialect.PG
)