package io.vertx.ext.binlog.mysql.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.binlog.mysql.BinlogClientOptions;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class SchemaResolver {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private Future lock = Future.succeededFuture();

  private HashMap<String, List<String>> tableColumns = new HashMap<>();

  private AsyncSQLClient sqlClient;

  SchemaResolver(Vertx vertx, BinlogClientOptions options) {
    //use a single connection to query information_schema
    sqlClient = MySQLClient.createNonShared(vertx,
      new JsonObject()
        .put("host", options.getHost())
        .put("port", options.getPort())
        .put("database", "information_schema")
        .put("maxPoolSize", 1)
        .put("username", options.getUsername())
        .put("password", options.getPassword())
    );
  }

  void getColumns(String schema, String table, Handler<List<String>> handler) {
    //chain the future to make sure the following queries is in order
    lock = lock
      .compose((res) -> {
        if (tableColumns.containsKey(table)) {
          handler.handle(tableColumns.get(table));
          return Future.succeededFuture();
        }
        return Future.<SQLConnection>future((f) ->
          sqlClient.getConnection(f))
          .compose((conn ->
            Future.<ResultSet>future((f) ->
              conn.query("SELECT COLUMN_NAME AS name from COLUMNS " +
                "WHERE TABLE_SCHEMA = '" + schema + "' " +
                "AND TABLE_NAME = '" + table + "' " +
                "ORDER BY ORDINAL_POSITION", f)
            )
              .map(rs -> {
                conn.close();
                if (rs.getNumRows() == 0) {
                  return null;
                }
                return rs
                  .getRows()
                  .stream()
                  .map(json -> json.getString("name"))
                  .collect(Collectors.toList());
              })
              .otherwise(e -> {
                conn.close();
                return null;
              })
          ))
          .map(columns -> {
            if (logger.isDebugEnabled()) {
              logger.debug("Fetch columns for table `{}` [{}]", table,
                Optional
                  .ofNullable(columns)
                  .orElse(Collections.emptyList())
                  .stream()
                  .collect(Collectors.joining(", "))
              );
            }
            tableColumns.put(table, columns);
            handler.handle(columns);
            return null;
          })
          .otherwise((e) -> {
            logger.error("Failed to fetch columns for table `{}`", table, e);
            handler.handle(null);
            return null;
          });
      });
  }

  void clearColumns() {
    tableColumns.clear();
  }

}
