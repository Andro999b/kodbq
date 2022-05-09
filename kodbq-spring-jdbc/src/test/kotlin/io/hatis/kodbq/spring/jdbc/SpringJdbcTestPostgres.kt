package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.SqlDialect

class SpringJdbcTestPostgres: SpringJdbcTest(
    "jdbc:tc:postgresql:14.2:///test?TC_INITSCRIPT=file:../sql/postgres.sql",
    SqlDialect.PG
)