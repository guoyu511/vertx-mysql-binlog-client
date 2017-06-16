# MySQL Binlog client for Vert.x

[![Travis CI](https://travis-ci.org/guoyu511/vertx-mysql-binlog-client.svg?branch=master)](https://travis-ci.org/guoyu511/vertx-mysql-binlog-client)

A Vert.x client allowing applications tapping into MySQL replication stream. Based on Vert.x 3.4.1.

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
    <version>0.1.0</version>
</dependency>
```

### Tapping into replication stream

In order to connect to MySQL as a slave, you need a `BinlogClient` instance first.

You can create a client specifying a `BinlogClientOptions`:

```java
BinlogClient binlogClient = BinlogClient.create(vertx, binlogClientOptions);
```

The `BinlogClientOptions` containing the following values:

* `host` the MySQL server host name. Default is `localhost`
* `port` the MySQL server port. Default is `3306`
* `username` the username. Default is `root`
* `password` the user's passowrd. Default is null means NO Password.
* `filename` the binlog file where to starts. Default is the current file as master
* `position` the binlog position where to starts. Default is the current position as master
* `keepAlive` enable "keep alive" feature on this client. Default is true
* `keepAliveInterval` "keep alive" interval in milliseconds. Default is 1000
* `heartbeatInterval` heartbeat interval in milliseconds. Default is 0 means no heartbeat

Be sure that the user **must** has the `REPLICATION CLIENT` privilege for the given schema.

You can then connect to the MySQL master with the method `connect`.

It happens asynchronously and the client may not be connected until some time after the call has returned:

```java
binlogClient.connect();
```

You can also supplying a handler which will be called after the connection established (or failed).

```
binlogClient.connect((ar) -> {
  ar.succeeded() // true if connection established
});
```

After connected to the MySQL master as a slave, the client can handle events now.

### Handle Row Events

There were several types of event defined in MySQL binlog protocol. For this client, it only concerned about events related to data modification such as `write`, `update` and  `delete`. All the events are presented as `JsonObject`. 

You can set a handler to the client to handle those types of events.

```java
binlogClient.handler((event) -> {
  String type = event.getString("type");
});
```

For a data modification event (write / update / delete) the `JsonObject` will be looks like that:

```json
{
  "type" : "write",
  "schema" : "test_db",
  "table" : "test_table",
  "row" : {
    "id" : 1000,
    "name" : "guoyu"
  }
}
```

The row event containing the following values:

* `type` the event type, should be one of `write`, `update`, `delete`
* `schema` the database which the data changed in
* `table` the table name which the row changed in
* `row` the row data in Json, columns as key / value pair.

**Column mapping**

The origin `ROW Events` sent by MySQL master contains the column index but not the column name.

Therefore, the BinlogClient use a `MySQLClient` instance to query the column index and names from `information_schema` database when received a `ROW Events` of a table for first time. Then it cache the column mapping and build the event object with them.

When there is any `DROP TABLE`, `ALTER TABLE` or `CREATE TABLE` event recevied, the mapping cached will be cleared and the BinlogClient will requery the column mapping for the subsequent `ROW Events`.


### Using as ReadStream

The BinlogClient implemented `ReadStream<JsonObject>` interface, that means all the methods provided by the `ReadStream` are available. 

For example, use `pause ` to pause reading (that will stop to read from the underlying InputStream) .

```java
binlogClient.pause();
```

use `resume` to continue:

```java
binlogClient.resume();
```

Even using `Pump` to pump the events to another `WriteStream`:

```java
Pump.pump(binlogClient, targetStream);
```

### Binlog Filename and position

Some time you need to know the filename and the position where the replication stream at. 

For example, you may want to save the filename and position when a event comes.

You can retrieve them use `filename` and `position` :

```java
binlogClient.handle((event) -> {
  //some event coming
  String filename = binlogClient.filename();
  long position = binlogClient.position();
  //save them in any way for future use
});

```

Next time when you create your client, you can pass the filename and position to the `BinlogClientOptions` to let the client to connect at the specified position. 

```java
binlogClientOptions.setFilename(filename);
binlogClientOptions.setPosition(position);
BinlogClient binlogClient = BinlogClient.create(vertx, binlogClientOptions);
```

This ensures that you will not lose any events.

### Closing the client

You can hold on to the client for a long time (e.g. the life-time of your verticle).

Once you have finished with it, you should close it:

```java
binlogClient.close();
// or close with a callback handler
binlogClient.close((ar) -> {});
```


## Running the Tests

You can run tests with a specified MySQL instance:

```
% mvn test -Dbinlog.host=[host] -Dbinlog.port=[port] -Dbinlog.user=[user] -Dbinlog.password=[password] -Dbinlog.schema=[schema]
```

The user must has `ALL ` privileges for the given schema.
