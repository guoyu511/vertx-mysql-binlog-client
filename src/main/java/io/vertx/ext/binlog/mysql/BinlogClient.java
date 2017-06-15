package io.vertx.ext.binlog.mysql;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.binlog.mysql.impl.BinlogClientImpl;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public interface BinlogClient extends ReadStream<JsonObject> {

  static BinlogClient create(Vertx vertx, BinlogClientOptions options) {
    return new BinlogClientImpl(vertx, options);
  }

  BinlogClient start();

  BinlogClient start(Handler<AsyncResult<Void>> startHandler);

  BinlogClient stop();

  BinlogClient stop(Handler<AsyncResult<Void>> stopHandler);

  @Override
  BinlogClient exceptionHandler(Handler<Throwable> handler);

  @Override
  BinlogClient handler(Handler<JsonObject> handler);

  @Override
  BinlogClient pause();

  @Override
  BinlogClient resume();

  @Override
  BinlogClient endHandler(Handler<Void> endHandler);

  boolean started();

  /**
   * The event bus address where the message sent to.
   *
   * @return event bus address
   */
  String address();

}
