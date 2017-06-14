# MySQL Binlog client for Vert.x

A Vert.x client allowing applications tapping into MySQL replication stream.

It uses [MySQL Binary Log connector](https://github.com/shyiko/mysql-binlog-connector-java) to interact with the MySQL which implemented the MySQL binlog protocol by java.

## How to use

### Configure your MySQL master

Be sure the binlog is enabled on the MySQL master and it is in **ROW** format, otherwise the client cannot receive any row events.

To enable the binary log, start the server with the [--log-bin=base_name](https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html#option_mysqld_log-bin) option. For example:

```
mysqld --log-bin=mysql-bin --binlog-format=ROW
```

To specify the format globally for all clients, set the global value of the [binlog_format](https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html#sysvar_binlog_format) system variable:

```
SET GLOBAL binlog_format = 'ROW';
```

### Using Binlog Client

Get the latest JAR(s) from here. Alternatively you can include following Maven dependency (available through Maven Central):

```xml
<dependency>
    <groupId>io.github.guoyu511</groupId>
    <artifactId>vertx-mysql-binlog</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Tapping into replication stream

In order to connect to MySQL as a slave, you need a `BinlogClient` instance first.

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
* `heartbeatInterval` heartbeat interval in milliseconds. Default is 0 means no heartbeat

Be sure that the user **must** has the `REPLICATION CLIENT` privilege for the given schema.

You can then connect to the MySQL master with the method `start`. 

It happens asynchronously and the client may not be connected until some time after the call has returned:

```java
binlogClient.start();
```

You can also supplying a handler which will be called after the connection established (or failed).

```
binlogClient.start((ar) -> {
  ar.succeeded() // true if connection established
});
```

After connected to the MySQL master as a slave, the client can handle events now.

### Handle Events

There were several types of event defined in MySQL binlog protocol. For this client, it only concerned about events related to data modification such as `write`, `update` and  `delete`. All the events are presented as `JsonObject`. 

You can set a handler to the client to handle those types of events.

```java
binlogClient.handler((event) -> {
  String type = event.getString("type");
});
```

For a data modification event (write / update / delete) the `JsonObject` will be looks like that:

```Json
{
  "type" : "write", //or update or delete
  "schema" : "test_db", //database name
  "table" : "test_table", //table name
  "row" : { //the row data
    "id" : 1000
    "name" : "tim"
    // some other fields
  }
}
```






### Using as ReadStream





### Binlog File and position





## Running the Tests

You can run tests with a specified MySQL instance:

```
% mvn test -Dbinlog.host=[host] -Dbinlog.port=[port] -Dbinlog.user=[user] -Dbinlog.password=[password] -Dbinlog.schema=[schema]
```

The user must has `ALL ` privileges for the given schema.

Be sure that the binlog is in **ROW** format, otherwise client cannot receive any row events.

To set binlog format to `ROW` using:

```
SET GLOBAL binlog_format = 'ROW';
```