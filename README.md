# MySQL Binlog Listener for Vert.x

A Vert.x client allowing applications tapping into MySQL replication stream.

# Getting Started

Please see the in source asciidoc documentation or the main documentation on the web-site for a full description:

* [Java in-source docs](../master/src/main/asciidoc/java/index.adoc)

# Running the tests

You can run tests with a specified MySQL instance:

```
% mvn test -Dbinlog.host=[host] -Dbinlog.port=[port] -Dbinlog.user=[user] -Dbinlog.password=[password] -Dbinlog.schema=[schema]
```

The user must has enough privileges of the given schema, at least `CREATE`, `DROP`, `SELECT`, `INSERT`, `UPDATE` and `REPLICATION CLIENT`.

**Be sure that the binlog format is `ROW` otherwise client cannot receive any row events.**

To set binlog format using:

```
SET GLOBAL binlog_format = 'ROW';
```