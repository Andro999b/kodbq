package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.SqlDialect

class SpringJdbcTestMSSql: SpringJdbcTest(
    "jdbc:tc:sqlserver:2017-latest:///test?TC_INITSCRIPT=mssql.sql",
    SqlDialect.MS_SQL
)