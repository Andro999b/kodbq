[![](https://jitpack.io/v/Andro999b/kotlin-database-utils.svg)](https://jitpack.io/#Andro999b/kotlin-database-utils)

### Kotlin DSL Sql Builder

Toolkit project that utilize kotlin dsl functional to generate sql

### Installation

You can check out the latest version here:  
https://jitpack.io/#Andro999b/kotlin-database-utils

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation("com.github.Andro999b.kotlin-database-utils:database-utils-fluent-jdbc:${version}")
    // or implementation("com.github.Andro999b.kotlin-database-utils:database-utils-quarkus-reactive:${version}")
    // or implementation("com.github.Andro999b.kotlin-database-utils:database-utils-quarkus-coroutines:${version}")
}
```


#### SQL Lib integrations
[fluent-jdbc](https://github.com/zsoltherpai/fluent-jdbc)  
[quarkus-reactive](https://github.com/zsoltherpai/fluent-jdbc)

TODO:  
jdbc prepared statements  
vertx io

### Examples
#### Insert

```kotlin
sqlInsert("table_name") {
    values {
        column("name", "values") 
        columns(mapOf("name" to "values")) // from map
        columns("name" to "values") // from pairs list
    }
}
```

#### Batch Insert

```kotlin
sqlInsert("table_name") {
    entities.forEach { entity ->
        values {
            column("name", entity.name)
        }
    }
}
```

#### Basic query 

```kotlin
sql {
    query("select ${c("id")} from ${t("t1")} where f = ${v(1)}")
}
```

#### Select All
```kotlin
sqlSelect("table_name"){}
```

#### Select Fields
```kotlin
sqlSelect("table_name") {
    returns("name")
}
```
#### Distinct Select
```kotlin
sqlSelect("table_name") {
    distinct()
}
```
#### Where cause
```kotlin
sqlSelect("table_name") {
    where {
        // all cause joins with "and" condition here
        id(id) // short cut for returning by id
        column("name", "sam") // name == "same"
        column("num", listOf(1,2,3)) // num in (1,2,3)
        column("ne", WhereOp.ne, "1") // ne != 1
        columnIsNull("deleted")
        columnInNotNull("created")
        columns(
            "c1" to "value", // ==
            "c2" to listOf(1,2,3), // "in"
            "c3" to null // is null
        )
        and { // explict and join
            // ...
        }
        or { // or join
            // ...
        }
    }
}
```
#### Update
```kotlin
sqlUpdate("table_name") {
    set {
        // same to insert values
    }
    where {
        // where syntax
    }
}
```
#### Delete
```kotlin
sqlDelete("table_name") {
    where {
        // where syntax
    }
}
```
#### Sort and limit
```kotlin
sqlSelect("table_name") {
    sort("column", asc = false)
    offset(10)
    limit(100)
}
```
#### range (offset + limit)
```kotlin
sqlSelect("table_name") {
    range(50, 100) // offset 50, limit 50
}
```

#### Aggregations
```kotlin
sqlSelect("table_name") {
    aggregation {
        count("count") // count(*) as "count"
        sum("num_field", "sum") // sum("num_field") as "sum"
        avg("num_field", "avg") //avg("num)field") as "avg"
    }
}
```
#### GroupBy
```kotlin
sqlSelect("table_name") {
    aggregation {
        groupBy("by_column")
        count("count")
    }
}
```
#### Joins
```kotlin
sqlSelect("table_name") {
    join("second_table", "second_table_column") { on("root_table_column") }
    leftJoin("third_table", "third_table_column") { on("second_table", "second_table_column_2") }
    where {
        table("second_table") {
            // where syntax
        }
    }
    sort("second_table", "second_table_sort_column")
    aggregation {
        table("second_table") {
            groupBy("by_column")
            sum("second_table_num_column", "sum")
        }
    }
}
```

#### Sql Insertion
You can insert some custom sql construction you SqlGenerator syntax:  
```kotlin
sqlInsert("test") {
    values {
        columnSql("column1") {
            insertSql("${column()} = ${value(1)}")
        }
        columnSql("column2") {
            insertSql("current_timestamp")
        }
    }
}

sqlUpdate("test") {
    set {
        columnSql("column1") {
            updateSql("${column()} = ${value(1)}")
        }
        columnSql("column2") {
            updateSql("current_timestamp")
        }
    }
}

sqlSelect("test") {
    where {
        columnSql("column") {
            whereSql("${column()} = ${value(1)}")
        }
    }
}
```

### Fluent JDBC support

Yo can use fluent jdbc lib for execute and map results

```kotlin
dependencies {
    implementation("io.hatis:database-utils-fluent-jdbc:${version}")
}
```

Examples:  
```kotlin
sqlSelect("test") {
    // query
}
    .build(fluentJdbc)
    .firstResult()


insertSelect("test") {
    // query
}
    .execute(fluentJdbc)
```

### Quarkus Reactive support

You can execute query with reactive client and get results as:

```kotlin
sqlSelect("test") {
    // query
}
    .execute(client)
```

#### Quarkus Reactive transactions with kotlin coroutines

Library allow to use transactions with kotlin coroutines in quarkus projects. 
To achieve you add dependency to you project: 
```kotlin
implementation("com.github.Andro999b.kotlin-database-utils:database-utils-quarkus-coroutines:${version}")
```
Then create transactions actions bean:
```kotlin
@Dependent
class TxActionsConfig {

    @Produces
    fun createTxActions(pool: PgPool) = CoroutineTxActions(pool)
}
```

now you can create transactions scope and execute queries in it:

```kotlin
txActions.withTx {
    //...transaction scope
}

// or
txActions.withTxUni {
    //...transaction scope
} // returns Uni

//... somewhere inside transaction scope

CoroutineTxActions.inTransaction { tx ->
    sqlSelect("test") {
        // query
    }
        .await(tx)
        // .awaitFirst(tx) { row -> Pojo() }
        // .awaitAll(tx) { row -> Pojo() }
}
```


