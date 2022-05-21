# KoDBQ - Kotlin DataBase Query

KoDBQ is DSL syntax library for generating sql queries.
Library build around idea to have just simple sql generator that just output sql query and positioning/named parameters that can be used with any other DB library framework.
Library also provide ready to use integration with various db libs/frameworks.

[![Testing](https://github.com/Andro999b/kodbq/actions/workflows/testing.yaml/badge.svg)](https://github.com/Andro999b/kodbq/actions/workflows/testing.yaml)
[![](https://jitpack.io/v/Andro999b/kodbq.svg)](https://jitpack.io/#Andro999b/kodbq)

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
Build some query

```kotlin
val (sql, params) = 
    sqlSelect("users") {
        where { id(1) }
    }
    .buildSqlAndParams()
```

or execute it(with jdbc integration)
```kotlin
val resultSet =
    sqlSelect("users") {
        where { id(1) }
    }
    .execute(datasource)
```

all integration provide `execute` or `build` methods

## DSL Syntax example

### Select all

```kotlin
sqlSelect("users")
```

### Select by id
```kotlin
sqlSelect("users") { 
    where { id(1L) } 
}
```

### Select with filters
```kotlin
sqlSelect("users") {
    where {
        column("name", "Bob")
        column("age") gt 18 // gt, lt, gte, lte, eq, not, like, `in`
        column("deleted").isNull() // isNotNull()
    }
}
```

### Columns list to return
```kotlin
sqlSelect("users") {
    returns {
        columns("age", "name")
        column("age", "user_age") // alias
        columns(
            "age" to "user_age",
            "name" to "user_name"
        ) // multiple fields with
        count("users_count") // max, min, avg, sum
        function("upper", "name", "upper_name") // upper(name) as upper_name
    }
}
```

### Select with filter equals by multiple fields
```kotlin
sqlSelect("users") {
    where {
        columns(
            "age" to 18,
            "name" to "Alice",
            "deleted" to null
        )
        // or
        columns(mapOf(
            "age" to 18,
            "name" to "Alice",
            "deleted" to null
        ))
    }
}
```

### Filter with `and` & `or`

several `colunm`/`colunms` call are works as `and` condition so 
```kotlin
sqlSelect("users") {
    where {
        column("age", 18)
        column("name", "Bob")
    }
}
```
will be converted to `"age"=18 and "name"='Bob'`  
for `or` condition you need specify it directly with `or` block
```kotlin
sqlSelect("users") {
    where {
        column("age", 18)
        or {
            column("age", 90) 
        }
    }
}
```
will be converted to `"age"=19 or "age"=90`  
but if we want to filter all user with name "Bob" and age 18 or 90 this condition will not work 
```kotlin
sqlSelect("users") {
    where {
        column("name", "Bob")
        column("age", 18)
        or {
            column("age", 90) 
        }
    }
}
```
it will generate `("name" = "Bob" and "age"=19) or "age"=90`. In this case you need use `and` block
```kotlin
sqlSelect("users") {
    where {
        column("name", "Bob")
        and {
            column("age", 18)
            or {
                column("age", 90)
            }
        }
    }
}
```

### Sort
```kotlin
sqlSelect("users") {
    sort {
        asc("created", "age") // asc sort 
        desc("name") // desc sort
        count() // sort by agg function in asc order. count(asc = false) -- desc order
    }
}
```

### Limit and offset
```kotlin
sqlSelect("users") {
    offset(10)
    limit(20)
    // or 
    range(10, 30)
}
```
**NOTE:** When working with MS_SQL dialed it is mandatory to specify sort:
```kotlin
sqlSelect("users", dialect = SqlDailect.MS_SQL) {
    sort("id")
    offset(10)
    limit(20)
}
```

### Distinct
```kotlin
sqlSelect("users") {
    distict()
}
```

### Group By
```kotlin
sqlSelect("users") {
    groupBy("age")
    // sort by multiple fields
    groupBy("age", "name")
}
```
**NOTE:** All columns names passed to group be will be also adder to returning columns.  
E.G. first query will produce: `select "age" from "users" group by "age"`


### Having
```kotlin
sqlSelect("users") {
    groupBy("name")
    having {
        min("age") gt 18
        column("deleted").isNull
    }
}
```


### Table joins 
```kotlin
sqlSelect("users") {
    // inner join
    join("orders", "user_id") on "id"
    // left join
    leftJoin("orders", "user_id") on "id"
    // right join
    rightJoin("orders", "user_id") on "id"
    // full join
    fullJoin("orders", "user_id") on "id" 
    // join third table
    join("orders_status", "order_id").on("orders", "id")
    
    // return columns of joined tables
    returns {
        table("orders") {
            columns("price")
        }
    }
    
    // filter by joined table
    where {
        table("orders") { column("price") gt 100 }
        or {
            table("orders") { column("price") lt 10 }
        }
    }
    
    // sort by joined table
    sort {
        table("orders") { asc("price") }
    }
    
    // group by joined table
    groupByTable("orders", "user_id")
    
    // having with joined table
    having {
        table("orders") {
            count() gt 1
        }
    }
}
```

### Insert
```kotlin
sqlInsert("users") {
    values {
        column("name", "Alice")
        columns(
            "age" to 18,
            "gender" to "female"
        )
    }
    // returns generated keys (functionality depends on db driver)
    generatedKeys("id")
}
```

### Batch Insert
```kotlin
sqlInsert("users") {
    users.forEach { user ->
        values {
            column("name", user.name)
            columns(
                "age" to user.age,
                "gender" to user.gender
            )
        }
    }
}
```

### Delete 
```kotlin
sqlDelete("users") {
    where { // same dsl filter as in sqlSelect
        column("name") eq "Jhon"
    }
}
```

### Delete All
```kotlin
sqlDelete("users")
```

### Update
```kotlin
sqlUpdate("users") {
    values {
        column("age", 60)
    }
    where { // same dsl filter as in sqlSelect
        column("name") eq "Jake"
    }
}
```

### Update All
```kotlin
sqlUpdate("users") {
    values {
        column("deleted", null)
    }
}
```

### Native sql
There is couple places where you can write your one sql part
```kotlin
sqlSelect("users") {
    returns {
        native { 
            // return true/false if column is null
            "${c("deleted")} is null as deleted"
            // inside all native block context methods props available:
            // c("colName")/column("colName") - return full escaped column name. E.g: "tableName"."colName"
            // t("tableName")/table("tableName") - return escaped table name
            // dialect - SQL dialect
            // v(1)/value(1) - add query parameter value
        }
    }
    where {
        native {
            // custom condition
            "upper(${c("name")})=${v("BOB")}"
        }
    }
    having {
        native {
            // custom having filter
            "count(*) > 1"
        }
    }
}

sqlInsert("users") {
    values {
        native("deleted") { // for insert and update you need specify column name
            "now()"
            // some addition properties available here:
            // c/column - name of update/insert column
            // usage - INSERT/UPDATE
        }
    }
}

sqlUpdate("users") {
    set {
        native("deleted") { // for insert and update you need specify column name
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
    "select * from ${t("users")} where ${c("name")}=${v("Bob")}"
}
```


### Sql Dialect
By default, library generate syntax compatible with SQL92 specification.
By for some DB you will need set SQL Dialect (MSQL). There is 2 ways to specify sql dialect:

Globally:

```kotlin
kodbqDialect = SqlDialect.MS_SQL
```

or per request:

```kotlin
sqlSelect("users", dialect = SqlDialect.MY_SQL)
```
