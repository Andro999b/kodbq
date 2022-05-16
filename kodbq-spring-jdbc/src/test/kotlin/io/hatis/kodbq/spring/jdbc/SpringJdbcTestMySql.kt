package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.SqlDialect

class SpringJdbcTestMySql: SpringJdbcTest(
    "jdbc:tc:mysql:8.0:///test?TC_INITSCRIPT=mysql.sql",
    SqlDialect.MY_SQL
)