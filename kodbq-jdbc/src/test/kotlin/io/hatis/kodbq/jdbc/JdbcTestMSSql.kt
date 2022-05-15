package io.hatis.kodbq.jdbc

import io.hatis.kodbq.SqlDialect

class JdbcTestMSSql: JdbcTest(
    "jdbc:tc:sqlserver:2017-latest:///test?TC_INITSCRIPT=mssql.sql",
    SqlDialect.MS_SQL
)