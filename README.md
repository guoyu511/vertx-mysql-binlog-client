# MySQL Binlog client for Vert.x

A Vert.x client allowing applications tapping into MySQL replication stream.

It uses [MySQL Binary Log connector](https://github.com/shyiko/mysql-binlog-connector-java) to interact with the MySQL which implemented the MySQL binlog protocol.

## Using the MySQL Binlog client

Get the latest JAR(s) from here. Alternatively you can include following Maven dependency (available through Maven Central):

```xml
<dependency>
    <groupId>io.github.guoyu511</groupId>
    <artifactId>vertx-mysql-binlog</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Getting Started

In order to connect to MySQL as a slave node, you need a `BinlogClient` instance first.

You can create a client specifying a `BinlogClientOptions`:

```java
BinlogClient binlogClient = BinlogClient.create(vertx, binlogClientOptions)
```

The `BinlogClientOptions` containing the following values:

* `host` the MySQL server host name. Default is `localhost`
* `port` the MySQL server port. Default is `3306`
* `username` the username. Default is `root`
* `password` the user's passowrd
* `schema` the database schema which tap into
* `binlogFilename` the binlog file where to starts. Default is the current file as master
* `binlogPosition` the binlog position where to starts. Default is the current position as master
* `keepAlive` enable "keep alive" feature on this client. Default is true
* `keepAliveInterval` "keep alive" interval in milliseconds. Default is 1000
* `heartbeatInterval` heartbeatInterval interval in milliseconds. Default is 0 means no heartbeat


## Running the tests

You can run tests with a specified MySQL instance:

```
% mvn test -Dbinlog.host=[host] -Dbinlog.port=[port] -Dbinlog.user=[user] -Dbinlog.password=[password] -Dbinlog.schema=[schema]
```

The user must has enough privileges of the given schema, at least `CREATE`, `DROP`, `SELECT`, `INSERT`, `UPDATE` and `REPLICATION CLIENT`.

Be sure that the binlog is in **ROW** format, otherwise client cannot receive any row events.

To set binlog format to `ROW` using:

```
SET GLOBAL binlog_format = 'ROW';
```