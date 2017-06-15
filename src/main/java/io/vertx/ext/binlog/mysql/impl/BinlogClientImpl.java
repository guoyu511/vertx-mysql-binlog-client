package io.vertx.ext.binlog.mysql.impl;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;

import java.io.IOException;
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

  private volatile boolean running = false;

  private volatile boolean pending = false;

  private String messageAddress;

  private Lock lock = new ReentrantLock();

  private Condition pendingCondition = lock.newCondition();

  private Context context;

  private EventDispatcher dispatcher;

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
    dispatcher = new EventDispatcher(vertx, options, messageAddress);
    client = new BinaryLogClient(
      host, port,
      options.getSchema(),
      options.getUsername(),
      options.getPassword()
    );
    if (options.getBinlogFilename() != null) {
      client.setBinlogFilename(options.getBinlogFilename());
    }
    if (options.getBinlogPosition() != -1) {
      client.setBinlogPosition(options.getBinlogPosition());
    }
    client.setHeartbeatInterval(options.getHeartbeatInterval());
    client.setKeepAlive(options.isKeepAlive());
    client.setKeepAliveInterval(options.getKeepAliveInterval());
    client.registerEventListener(this::handle);
  }

  public BinlogClientImpl start() {
    return start((ar) -> {
    });
  }

  public BinlogClientImpl start(Handler<AsyncResult<Void>> startHandler) {
    if (running) {
      throw new IllegalStateException("Client already started.");
    }
    running = true;
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
        running = false;
        if (exceptionHandler != null) {
          exceptionHandler.handle(ar.cause());
        }
      }
      startHandler.handle(ar);
    });
    return this;
  }

  public BinlogClientImpl stop() {
    return stop((ar) -> {
    });
  }

  public BinlogClientImpl stop(Handler<AsyncResult<Void>> stopHandler) {
    if (!running) {
      throw new IllegalStateException("Client is not started.");
    }
    running = false;
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
        stopHandler.handle(ar);
      } else {
        stopHandler.handle(ar);
      }
    });
    return this;
  }

  public BinlogClientImpl exceptionHandler(Handler<Throwable> handler) {
    this.dispatcher.exceptionHandler(handler);
    this.exceptionHandler = handler;
    return this;
  }

  public BinlogClientImpl handler(Handler<JsonObject> handler) {
    this.dispatcher.handler(handler);
    return this;
  }

  public BinlogClientImpl pause() {
    if (!running) {
      throw new IllegalStateException("Client is not started.");
    }
    this.pending = true;
    return this;
  }

  public BinlogClientImpl resume() {
    if (!running) {
      throw new IllegalStateException("Client is not started.");
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

  public BinlogClientImpl endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public boolean started() {
    return running;
  }

  @Override
  public String address() {
    return messageAddress;
  }

  public BinaryLogClient getClinet() {
    return client;
  }

  //this method will be called on blc thread
  private void handle(Event eventSrc) {
    if (!running) {
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
