package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.SqlDialect

class MutinyVertxJDBCTestPostgres: MutinyVertxJDBCTest(
    "jdbc:tc:postgresql:14.2:///test?TC_INITSCRIPT=file:../sql/postgres.sql",
    SqlDialect.PG
)