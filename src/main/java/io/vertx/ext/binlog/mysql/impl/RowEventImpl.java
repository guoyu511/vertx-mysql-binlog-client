package io.vertx.ext.binlog.mysql.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.binlog.mysql.RowEvent;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RowEventImpl implements RowEvent {

  private String schema;

  private String type;

  private String table;

  private JsonObject body;

  RowEventImpl(String schema, String table, String type, JsonObject body) {
    this.schema = schema;
    this.type = type;
    this.table = table;
    this.body = body;
  }

  public String schema() {
    return schema;
  }

  public String table() {
    return table;
  }

  public String type() {
    return type;
  }

  public JsonObject body() {
    return body;
  }

}
