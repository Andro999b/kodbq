# KoDBQ - Kotlin DataBase Query

KoDBQ is DSL syntax library for generating sql queries.
Library build around idea to have just simple sql generator that just output sql query and positioning/named parameters that can be used with any other DB library framework.
Library also provide ready to use integration with various db libs/frameworks.

[![Testing](https://github.com/Andro999b/kodbq/actions/workflows/testing.yaml/badge.svg)](https://github.com/Andro999b/kodbq/actions/workflows/testing.yaml)
[![](https://jitpack.io/v/Andro999b/kodbq.svg)](https://jitpack.io/#Andro999b/kodbq)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Andro999b_kodbq&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Andro999b_kodbq)

# Table of Content
- [Supported Integrations](#supported-integrations)
- [Getting Started](#getting-started)
- [DSL Syntax example](#dsl-syntax-example)
    - [Define schema object](#define-schema-object)
    - [Select all](#select-all)
    - [Select by id](#select-by-id)
    - [Select with filters](#select-with-filters)
    - [Columns list](#columns-list)
    - [Sort](#sort)
    - [Limit and offset](#limit-and-offset)
    - [Distinct](#distinct)
    - [Group By](#group-by)
    - [Having](#having)
    - [Joins](#joins)
    - [Unions](#unions)
    - [Insert](#insert)
    - [Delete](#delete)
    - [Update](#update)
    - [Native sql](#native-sql)
- [SQL Dialect](#sql-dialect)

## Supported Integrations

- JDBC
- Spring JDBC
- Fluent JDBC
- Vertx JDBC
- Mutiny Vertx JDBC (Quarkus reactive)
- Vertx (Reactive clients)
- Mutiny Vertx (Quarkus reactive)
- Spring R2DBC

## Getting Started

Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
Add the dependency:
```groovy
dependencies {
    implementation "com.github.Andro999b:kodbq-jdbc:$version"
    implementation "com.github.Andro999b:kodbq-spring-jdbc:$version"
    implementation "com.github.Andro999b:kodbq-spring-r2dbc:$version"
    implementation "com.github.Andro999b:kodbq-fluent-jdbc:$version"
    implementation "com.github.Andro999b:kodbq-vertx:$version"
    implementation "com.github.Andro999b:kodbq-vertx-jdbc:$version"
    implementation "com.github.Andro999b:kodbq-mutiny-vertx:$version"
    implementation "com.github.Andro999b:kodbq-mutiny-vertx-jdbc:$version"
}
```
Latest version can be found at [Jitpack](https://jitpack.io/#Andro999b/kodbq)  

Define table schema object
```kotlin
object Users: Table("users") {
    val id = column("id")
}
```

Build some query

```kotlin
val (sql, params) = 
    sqlSelect(Users) {
        where { Users.id eq 1 }
    }
    .buildSqlAndParams()
```

or execute it(with jdbc integration)
```kotlin
val resultSet =
    sqlSelect(Users) {    
        where { Users.id eq 1 }
    }
    .execute(datasource)
```

all integration provide `execute` or `build` methods

## DSL Syntax example

### Define schema object

```kotlin
object Users : Table("users") { // table name
    val id = column("id") // column name
    val name = column("name")
    val age = column("age")
    val created = column("created")
    val deleted = column("deleted")
}

object Orders : Table("orders") {
    val id = column("id")
    val created = column("created")
    val userId = column("user_id")
    val price = column("price")
    val article = column("article")
    val userRef = userId refernce Users.id // table reference
}
```

### Select all

```kotlin
sqlSelect(Users)
```

### Select by id
```kotlin
sqlSelect(Users) { 
    where { Users.id eq 1L } 
}
```

### Select with filters
```kotlin
sqlSelect(Users) {
    where {
        Users.name eq "Bob"
        Users.age gt 18
        Users.delete.isNull() // isNotNull()
    }
}
```

Filter with `and` & `or`

several `colunm`/`colunms` call are works as `and` condition so
```kotlin
sqlSelect(Users) {
    where {
        Users.age eq 18
        Users.name eq "Bob"
    }
}
```
will be converted to `"age"=18 and "name"='Bob'`  
for `or` condition you need specify it directly with `or` block
```kotlin
sqlSelect(Users) {
    where {
        Users.age eq 18
        or {
          Users.age eq 90
        }
    }
}
```
will be converted to `"age"=19 or "age"=90`  
but if we want to filter all user with name "Bob" and age 18 or 90 this condition will not work
```kotlin
sqlSelect(Users) {
    where {
        Users.name eq "Bob"
        Users.age eq 18
        or {
            Users.age eq 90
        }
    }
}
```
it will generate `("name" = "Bob" and "age"=19) or "age"=90`. In this case you need use `and` block
```kotlin
sqlSelect(Users) {
    where {
        Users.name eq "Bob"
        and {
            Users.age eq 18
            or {
                Users.age eq 90
            }
        }
    }
}
```


### Columns list
```kotlin
sqlSelect(Users) {
    returns {
        columns(Users.name)
        column(Users.age, "user_age") // alias
        columns(
            Users.age to "user_age",
            Users.name to "user_name"
        ) // multiple fields with
        count("users_count") // max, min, avg, sum
        function("upper", Users.name, "upper_name") // upper(name) as upper_name
    }
}
```

### Sort
```kotlin
sqlSelect(Users) {
    sort {
        asc(Users.created, Users.age) // asc sort 
        desc(Users.name) // desc sort
        count() // sort by agg function in asc order. count(asc = false) -- desc order
    }
}
```

### Limit and offset
```kotlin
sqlSelect(Users) {
    offset(10)
    limit(20)
    // or 
    range(10, 30)
}
```
**NOTE:** When working with MS_SQL dialect it is mandatory to specify sort:
```kotlin
sqlSelect(Users, dialect = SqlDailect.MS_SQL) {
    sort{asc(Users.id)}
    offset(10)
    limit(20)
}
```

### Distinct
```kotlin
sqlSelect(Users) {
    distict()
}
```

### Group By
```kotlin
sqlSelect(Users) {
    groupBy(Users.age)
    // sort by multiple fields
    groupBy(Users.age, Users.name)
}
```
**NOTE:** All columns names passed to group be will be also adder to returning columns.  
E.G. first query will produce: `select "age" from "users" group by "age"`


### Having
```kotlin
sqlSelect(Users) {
    groupBy(Users.name)
    having {
        min(Users.age) gt 18
        Users.deleted.isNull()
    }
}
```


### Joins 
```kotlin
sqlSelect(Users) {
    // inner join
    join(Orders.userId) on Users.id
    // left join
    leftJoin(Orders.userId) on Users.id
    // right join
    rightJoin(Orders.userId) on Users.id
    // full join
    fullJoin(Orders.userId) on Users.id
    // join third table
    join(OrdersStatus.orderId).on(Orders.id)
    // join using ref
    join(Orders.userRef)
}
```

### Unions
```kotlin
sqlSelect(Users) {
    where { Users.id eq 1 }
    union(all = true) { // return 2 identical rows with union all
        where { Users.id eq 1 } // returns, groupBy, sort, having...
    }
    union(UsersArchive) { // union with another table
        where { UsersArchive.id eq 2 }
    }
}
```

### Insert
```kotlin
sqlInsert(Users) {
    values {
        Users.name to "Alice"
        Users.age to 18
        Users.gender to "female"
    }
    // returns generated keys (functionality depends on db driver)
    generatedKeys(Users.id)
}
```

Batch insert
```kotlin
sqlInsert(Users) {
    users.forEach { user ->
        values {
            Users.name to user.name
            Users.age to user.age
            Users.gender to user.gender
        }
    }
}
```

### Delete 
```kotlin
sqlDelete(Users) {
    where { // same dsl filter as in sqlSelect
        Users.name eq "Jhon"
    }
}
```

Delete all
```kotlin
sqlDelete(Users)
```

### Update
```kotlin
sqlUpdate(Users) {
    set {
        Users.age to 60
    }
    where { // same dsl filter as in sqlSelect
        Users.name eq "Jake"
    }
}
```

Update all
```kotlin
sqlUpdate(Users) {
    set {
        Users.deleted to null
    }
}
```

### Native sql
There is couple places where you can write your one sql part
```kotlin
sqlSelect(Users) {
    returns {
        native { 
            // return true/false if column is null
            "${c(Users.deleted)} is null as deleted"
            // inside all native block context methods props available:
            // c(ColumnDefinition)/column(ColumnDefinition) - return full escaped column name. E.g: "tableName"."colName"
            // t(Table)/table(Table) - return escaped table name
            // dialect - SQL dialect
            // v(1)/value(1) - add query parameter value
        }
    }
    where {
        native {
            // custom condition
            "upper(${c(Users.name)})=${v("BOB")}"
        }
    }
    having {
        native {
            // custom having filter
            "count(*) > 1"
        }
    }
}

sqlInsert(Users) {
    values {
        native(Users.deleted) { // for insert and update you need specify column name
            "now()"
            // some addition properties available here:
            // c/column - name of update/insert column
            // usage - INSERT/UPDATE
        }
    }
}

sqlUpdate(Users) {
    set {
        native(Users.deleted) { // for insert and update you need specify column name
            "now()"
            // some addition properties available here:
            // c/column - name of update/insert column
            // usage - INSERT/UPDATE
        }
    }
}
```

also you can use `sql` builder to implement some custom generation logic
```kotlin
sql {
    "select * from ${t(Users)} where ${c(Users.name)}=${v("Bob")}"
}
```


## SQL Dialect
By default, library generate syntax compatible with SQL92 specification.
But for some DB you will need set SQL Dialect (MSSQL,MySQL). There is 2 ways to specify sql dialect:

Globally:

```kotlin
kodbqDialect = SqlDialect.MS_SQL
```

or per request:

```kotlin
sqlSelect(Users, dialect = SqlDialect.MY_SQL)
```
