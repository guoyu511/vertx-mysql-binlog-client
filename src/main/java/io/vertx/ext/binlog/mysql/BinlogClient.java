package io.vertx.ext.binlog.mysql;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.binlog.mysql.impl.BinlogClientImpl;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
@VertxGen
public interface BinlogClient extends ReadStream<JsonObject> {

  /**
   * Create a binlog client with vert.x instance and options.
   * @param vertx a vert.x instance
   * @param options the options
   * @return the new client
   */
  static BinlogClient create(Vertx vertx, BinlogClientOptions options) {
    return new BinlogClientImpl(vertx, options);
  }

  /**
   * Connect to the MySQL master.
   * It happens asynchronously and the client may not be connected until some time after the call has returned.
   * @return client itself
   */
  @Fluent
  BinlogClient connect();

  /**
   * Connect to the MySQL master with a callback handler.
   * It which will be called after the connection established (or failed).
   * @return client itself
   */
  @Fluent
  BinlogClient connect(Handler<AsyncResult<Void>> startHandler);

  /**
   * Close the client
   * It happens asynchronously and the client may not be closed until some time after the call has returned.
   * @return client itself
   */
  @Fluent
  BinlogClient close();

  /**
   * Close the client - when it is fully closed the handler will be called.
   * @return client itself
   */
  @Fluent
  BinlogClient close(Handler<AsyncResult<Void>> closeHandler);

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

  /**
   * Determine the client is connected or not
   * @return connected or not
   */
  boolean connected();

  /**
   * The current filename of the binlog
   * @return binlog filename
   */
  String filename();
  /**
   * The current position of the binlog
   * @return binlog position
   */
  long position();

}
