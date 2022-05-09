package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.SqlDialect

class MutinyVertxTestPostgres: MutinyVertxTest(
    "jdbc:tc:postgresql:14.2:///test?TC_INITSCRIPT=file:../sql/postgres.sql",
    SqlDialect.PG
)