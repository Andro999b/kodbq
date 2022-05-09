package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.SqlDialect

class SpringJdbcTestMysql: SpringJdbcTest(
    "jdbc:tc:mysql:8.0:///test?TC_INITSCRIPT=file:../sql/mysql.sql",
    SqlDialect.MY_SQL
)