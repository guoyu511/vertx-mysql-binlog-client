package io.vertx.ext.binlog.mysql.impl;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import com.github.shyiko.mysql.binlog.event.RotateEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
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
class EventDispatcher {

  private String schema;

  private String messageAddress;

  private boolean publishMessage;

  private boolean sendMessage;

  private EventBus eventBus;

  private Handler<JsonObject> handler;

  private TableMapEventData lastTableMap;

  private Handler<Throwable> exceptionHandler;

  private AsyncSQLClient sqlClient;

  private HashMap<String, List<String>> tableColumns = new HashMap<>();

  private Future lock = Future.succeededFuture();

  private Logger logger = LoggerFactory.getLogger(getClass());

  EventDispatcher(Vertx vertx, BinlogClientOptions options, String messageAddress) {
    this.messageAddress = messageAddress;
    this.schema = options.getSchema();
    this.publishMessage = options.isPublishMessage();
    this.sendMessage = options.isSendMessage();
    this.eventBus = vertx.eventBus();
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

  void dispatch(com.github.shyiko.mysql.binlog.event.Event evt) {
    Object data = evt.getData();
    if (RotateEventData.class.isInstance(data)) {
      handleRotateEvent((RotateEventData)data);
    } else if (TableMapEventData.class.isInstance(data)) {
      handleTableMapEvent((TableMapEventData) data);
    } else if (EventType.isWrite(evt.getHeader().getEventType())) {
      handleWriteEvent((WriteRowsEventData) data);
    } else if (EventType.isUpdate(evt.getHeader().getEventType())) {
      handleUpdateEvent((UpdateRowsEventData) data);
    } else if (EventType.isDelete(evt.getHeader().getEventType())) {
      handleDeleteEvent((DeleteRowsEventData) data);
    } else if (QueryEventData.class.isInstance(data)) {
      handleQueryEvent((QueryEventData) data);
    }
  }

  void handler(Handler<JsonObject> handler) {
    this.handler = handler;
  }

  void exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  private void handleRotateEvent(RotateEventData data) {
    notify(new JsonObject()
      .put("type", "rotate")
      .put("filename", data.getBinlogFilename())
      .put("position", data.getBinlogPosition())
    );
  }

  private void handleTableMapEvent(TableMapEventData data) {
    lastTableMap = data;
  }

  private void handleWriteEvent(WriteRowsEventData evt) {
    if (lastTableMap == null) {
      throw new IllegalStateException("Missing table map event");
    }
    evt.getRows().forEach(row ->
      handleRowEvent(lastTableMap.getTable(), "write", Arrays.asList(row))
    );
    lastTableMap = null;
  }

  private void handleUpdateEvent(UpdateRowsEventData evt) {
    if (lastTableMap == null) {
      throw new IllegalStateException("Missing table map event");
    }
    evt.getRows().forEach(entry ->
      handleRowEvent(lastTableMap.getTable(), "update", Arrays.asList(entry.getValue()))
    );
    lastTableMap = null;
  }

  private void handleDeleteEvent(DeleteRowsEventData evt) {
    if (lastTableMap == null) {
      throw new IllegalStateException("Missing table map event");
    }
    evt.getRows().forEach(row ->
      handleRowEvent(lastTableMap.getTable(), "delete", Arrays.asList(row))
    );
    lastTableMap = null;
  }

  private void handleQueryEvent(QueryEventData data) {
    String sql = data.getSql().toUpperCase().trim();
    if (sql.startsWith("CREATE TABLE") ||
      sql.startsWith("DROP TABLE") ||
      sql.startsWith("ALTER TABLE")) {
      if (logger.isDebugEnabled())
        logger.debug("Handle DDL statement, clear column mapping");
      tableColumns.clear();
    }
  }

  private void handleRowEvent(String table, String type, List<Serializable> fields) {
    getColumns(table, columns -> {
      if (columns == null) {
        notifyException(new IllegalStateException("Missing table information " + schema + "." + table));
        return;
      }
      if (columns.size() != fields.size()) {
        notifyException(new IllegalStateException("Columns not matched, expect " + columns.size() + " got " + fields.size()));
        return;
      }
      Map<String, Object> row = IntStream.range(0, columns.size()).boxed()
        .collect(HashMap::new, (map, i) -> map.put(columns.get(i), fields.get(i)), HashMap::putAll);
      notify(new JsonObject()
        .put("table", table)
        .put("type", type)
        .put("row", new JsonObject(row)));
    });
  }

  private void getColumns(String table, Handler<List<String>> handler) {
    //chain the future to make sure the following queries is in order
    lock = lock
      .compose((res) -> {
        if (tableColumns.containsKey(table)) {
          handler.handle(tableColumns.get(table));
          return Future.succeededFuture();
        }
        return Future.<SQLConnection>future((f) ->
          sqlClient.getConnection(f
          ))
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
                return rs.getRows().stream()
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

  private void notify(JsonObject event) {
    event.put("schema", schema);
    if (handler != null) {
      handler.handle(event);
    }
    if (publishMessage) {
      eventBus.publish(
        messageAddress, event,
        new DeliveryOptions()
          .addHeader("schema", schema)
          .addHeader("type", event.getString("type"))
      );
    } else if (sendMessage) {
      eventBus.send(
        messageAddress, event,
        new DeliveryOptions()
          .addHeader("schema", schema)
          .addHeader("type", event.getString("type"))
      );
    }
  }

  private void notifyException(Throwable t) {
    if (exceptionHandler == null) {
      return;
    }
    exceptionHandler.handle(t);
  }

}
