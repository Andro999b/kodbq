# KoDBQ - Kotlin DataBase Query

KoDBQ is DSL syntax library for generating sql queries.
Library build around idea to have just simple sql generator that just output sql query and positioning/named parameters that can be used with any other DB library framework.
Library also provide ready to use integration with various db libs/frameworks.

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

add library to you project

build some query

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

all integration provide `execute` or `build` methods to queries

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

### Sort
```kotlin
sqlSelect("users") {
    sort("age")
    // desc sort
    sort("name", asc = false)
    // sort by multiple fields
    sort("created", "age")
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
    groupBy("age")
    // sort by multiple fields
    groupBy("age", "name")
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
        table("orders") {
            column("price") gt 10
        }
    }
    
    // sort by joined table
    sortByTable("orders", "price")
    
    // group by joined table
    groupByTable("orders", "user_id")
}
```

### Insert
```kotlin
sqlInsert("users") {
    values {
        column("name", "Alice")
        columns(mapOf(
            "age" to 18,
            "gender" to "female"
        ))
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
                mapOf(
                    "age" to user.age,
                    "gender" to user.gender
                )
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
sqlDelete("users") {
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
sqlDelete("users") {
    values {
        column("deleted", null)
    }
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