package io.vertx.ext.binlog.mysql;

import java.util.concurrent.TimeUnit;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
@DataObject(generateConverter = true)
public class BinlogClientOptions {

  private static final String DEFAULT_HOST = "localhost";

  private static final int DEFAULT_PORT = 3306;

  private static final String DEFAULT_USERNAME = "root";

  private static final String DEFAULT_PASSWORD = null;

  private static final long DEFAULT_CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

  private static final String DEFAULT_FILENAME = null;

  private static final long DEFAULT_POSITION = -1;

  private static final boolean DEFAULT_KEEP_ALIVE = true;

  private static final long DEFAULT_KEEP_ALIVE_INTERVAL= TimeUnit.MINUTES.toMillis(1);

  private static final long DEFAULT_HEARTBEAT_INTERVAL = 0;

  private String host = DEFAULT_HOST;

  private int port = DEFAULT_PORT;

  private String username = DEFAULT_USERNAME;

  private String password = DEFAULT_PASSWORD;

  private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;

  private String filename = DEFAULT_FILENAME;

  private long position = DEFAULT_POSITION;

  private boolean keepAlive = DEFAULT_KEEP_ALIVE;

  private long keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;

  private long heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;

  public BinlogClientOptions() {}

  public BinlogClientOptions(JsonObject json) {
    BinlogClientOptionsConverter.fromJson(json, this);
  }

  public long getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public BinlogClientOptions setHeartbeatInterval(long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
    return this;
  }

  public boolean isKeepAlive() {
    return keepAlive;
  }

  public BinlogClientOptions setKeepAlive(boolean keepAlive) {
    this.keepAlive = keepAlive;
    return this;
  }

  public long getKeepAliveInterval() {
    return keepAliveInterval;
  }

  public BinlogClientOptions setKeepAliveInterval(long keepAliveInterval) {
    this.keepAliveInterval = keepAliveInterval;
    return this;
  }

  public long getPosition() {
    return position;
  }

  public BinlogClientOptions setPosition(long position) {
    this.position = position;
    return this;
  }

  public String getFilename() {
    return filename;
  }

  public BinlogClientOptions setFilename(String filename) {
    this.filename = filename;
    return this;
  }

  public long getConnectTimeout() {
    return connectTimeout;
  }

  public BinlogClientOptions setConnectTimeout(long connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public String getHost() {
    return host;
  }

  public BinlogClientOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public BinlogClientOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public BinlogClientOptions setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public BinlogClientOptions setPassword(String password) {
    this.password = password;
    return this;
  }

}
