package io.vertx.ext.binlog.mysql.impl;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.binlog.mysql.BinlogClient;
import io.vertx.ext.binlog.mysql.BinlogClientOptions;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class BinlogClientImpl implements BinlogClient {

  Vertx vertx;

  BinaryLogClient client;

  private Logger logger = LoggerFactory.getLogger(BinlogClientImpl.class);

  private Handler<Void> endHandler;

  private Handler<Throwable> exceptionHandler;

  private String host;

  private int port;

  private long connectTimeout;

  private volatile boolean connected = false;

  private volatile boolean pending = false;

  private String messageAddress;

  private Lock lock = new ReentrantLock();

  private Condition pendingCondition = lock.newCondition();

  private Context context;

  private EventDispatcher dispatcher;

  private AsyncSQLClient sqlClient;

  private Thread shutdownHook = new Thread(() -> {
    try {
      if (client.isConnected())
        client.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
    }
  });

  public BinlogClientImpl(Vertx vertx, BinlogClientOptions options) {
    this.vertx = vertx;
    this.context = vertx.getOrCreateContext();
    this.host = options.getHost();
    this.port = options.getPort();
    this.connectTimeout = options.getConnectTimeout();
    this.messageAddress = UUID.randomUUID().toString();
    this.sqlClient = MySQLClient.createNonShared(vertx,
      new JsonObject()
        .put("host", options.getHost())
        .put("port", options.getPort())
        .put("database", "information_schema")
        .put("maxPoolSize", 1)
        .put("username", options.getUsername())
        .put("password", options.getPassword())
    );
    dispatcher = new EventDispatcher(vertx, options,
      new SchemaResolver(sqlClient), messageAddress);
    client = new BinaryLogClient(
      host, port,
      options.getUsername(),
      Optional.ofNullable(
        options.getPassword()
      ).orElse("")
    );
    if (options.getFilename() != null) {
      client.setBinlogFilename(options.getFilename());
    }
    if (options.getPosition() != -1) {
      client.setBinlogPosition(options.getPosition());
    }
    client.setHeartbeatInterval(options.getHeartbeatInterval());
    client.setKeepAlive(options.isKeepAlive());
    client.setKeepAliveInterval(options.getKeepAliveInterval());
    client.registerEventListener(this::handle);
  }

  @Override
  public BinlogClientImpl connect() {
    return connect((ar) -> {
    });
  }

  @Override
  public BinlogClientImpl connect(Handler<AsyncResult<Void>> startHandler) {
    if (connected) {
      throw new IllegalStateException("Client already connected.");
    }
    connected = true;
    vertx.<Void>executeBlocking((f) -> {
      try {
        client.connect(connectTimeout);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        f.complete();
      } catch (Exception e) {
        f.fail(e);
      }
    }, true, (ar) -> {
      if (ar.succeeded()) {
        if (logger.isDebugEnabled()) {
          logger.debug("Binlog listener " +
            "[" + host + ":" + port + "]" +
            " started ");
        }
      } else {
        connected = false;
        if (exceptionHandler != null) {
          exceptionHandler.handle(ar.cause());
        }
      }
      startHandler.handle(ar);
    });
    return this;
  }

  @Override
  public BinlogClientImpl close() {
    return close((ar) -> {
    });
  }

  @Override
  public BinlogClientImpl close(Handler<AsyncResult<Void>> closeHandler) {
    if (!connected) {
      throw new IllegalStateException("Client is not connected.");
    }
    sqlClient.close();
    connected = false;
    vertx.<Void>executeBlocking((f) -> {
      try {
        client.disconnect();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        if (logger.isDebugEnabled()) {
          logger.debug("Binlog listener " +
            "[" + host + ":" + port + "]" +
            " stopped ");
        }
        f.complete();
      } catch (IOException e) {
        logger.error("Disconnect failed", e);
        f.fail(e);
      }
    }, true, (ar) -> {
      if (ar.succeeded()) {
        if (endHandler != null)
          endHandler.handle(null);
        closeHandler.handle(ar);
      } else {
        closeHandler.handle(ar);
      }
    });
    return this;
  }

  @Override
  public BinlogClientImpl exceptionHandler(Handler<Throwable> handler) {
    this.dispatcher.exceptionHandler(handler);
    this.exceptionHandler = handler;
    return this;
  }

  @Override
  public BinlogClientImpl handler(Handler<JsonObject> handler) {
    this.dispatcher.handler(handler);
    return this;
  }

  @Override
  public BinlogClientImpl pause() {
    if (!connected) {
      throw new IllegalStateException("Client is not connected.");
    }
    this.pending = true;
    return this;
  }

  @Override
  public BinlogClientImpl resume() {
    if (!connected) {
      throw new IllegalStateException("Client is not connected.");
    }
    this.pending = false;
    try {
      lock.lock();
      pendingCondition.signal();
    } finally {
      lock.unlock();
    }
    return this;
  }

  @Override
  public BinlogClientImpl endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public boolean connected() {
    return connected;
  }

  @Override
  public String address() {
    return messageAddress;
  }

  @Override
  public String filename() {
    return client.getBinlogFilename();
  }

  @Override
  public long position() {
    return client.getBinlogPosition();
  }

  public BinaryLogClient getClient() {
    return client;
  }

  //this method will be called on blc thread
  private void handle(Event eventSrc) {
    if (!connected) {
      return;
    }
    try {
      lock.lock();
      if (pending) {
        pendingCondition.await();
      }
      context.runOnContext((v) ->
        dispatcher.dispatch(eventSrc)
      );
    } catch (Exception e) {
      context.runOnContext((v) -> {
        if (exceptionHandler != null) {
          exceptionHandler.handle(e);
        }
      });
    } finally {
      lock.unlock();
    }
  }

}
