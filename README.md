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

### Configure your MySQL master

Be sure that the MySQL master is enable the binlog file and the binlog is in **ROW** format, otherwise client cannot receive any row events.

To enable the binary log, start the server with the [--log-bin=base_name](https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html#option_mysqld_log-bin) option. For example:

```
mysqld --log-bin=mysql-bin --binlog-format=ROW
```

To specify the format globally for all clients, set the global value of the [binlog_format](https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html#sysvar_binlog_format) system variable:

```
SET GLOBAL binlog_format = 'ROW';
```


## Usage


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
* `heartbeatInterval` heartbeatInterval interval in milliseconds. Default is 0 means no heartbeat

You can then connect to the MySQL master with the method `start`. It happens asynchronously and the client may not be connected until some time after the call has returned.:

```java
binlogClient.start();
```

You can aslo supplying a handler which will be called after the connection established(or failed).

```
binlogClient.start((ar) -> {
  ar.succeeded() // true if connection established
});
```

### Handle row events


### Handle rotate events


### Using as ReadStream


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