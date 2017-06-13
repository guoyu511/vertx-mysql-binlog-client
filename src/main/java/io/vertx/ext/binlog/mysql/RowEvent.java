package io.vertx.ext.binlog.mysql;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public interface RowEvent {

  String schema();

  String table();

  String type();

  JsonObject body();

}
