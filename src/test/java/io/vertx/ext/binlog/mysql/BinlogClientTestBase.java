package io.vertx.ext.binlog.mysql;


import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import static java.lang.Integer.parseInt;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class BinlogClientTestBase extends VertxTestBase {

  private static List<Map.Entry<Integer, String>> rows;

  private static Connection conn;

  private static final String[] SQL = new String[]{
    "DROP TABLE IF EXISTS `binlog_client_test`",
    "CREATE TABLE `binlog_client_test` (`id` int(11) NOT NULL AUTO_INCREMENT,`name` varchar(100) NOT NULL,PRIMARY KEY (`id`));"
  };

  protected BinlogClient client;

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private static final int ROWS = 1000;

  @BeforeClass
  public static void init() throws Exception {
    conn = DriverManager.getConnection(
      config().getString("url"),
      config().getString("user"),
      config().getString("password")
    );
    rows = IntStream
      .iterate(1, i -> i + 1)
      .limit(ROWS)
      .boxed()
      .map(i -> new AbstractMap.SimpleEntry<>(i, UUID.randomUUID().toString()))
      .collect(Collectors.toList());
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    for (String sql : SQL) {
      conn.createStatement().execute(sql);
    }
    client = BinlogClient.create(vertx,
      new BinlogClientOptions()
        .setUsername(config().getString("user"))
        .setPassword(config().getString("password"))
        .setHost(config().getString("host"))
        .setPort(config().getInteger("port"))
        .setHeartbeatInterval(5000)
    );
    CountDownLatch latch = new CountDownLatch(1);
    client.connect(onSuccess((ignore) ->
      latch.countDown()
    ));
    awaitLatch(latch);
  }

  @Override
  public void after() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    if (client.connected()) {
      client.close((ignore) ->
        latch.countDown()
      );
      awaitLatch(latch);
    }
  }

  protected static JsonObject config() {
    JsonObject config = new JsonObject()
      .put("user", System.getProperty("binlog.user", "root"))
      .put("password", System.getProperty("binlog.password"))
      .put("host", System.getProperty("binlog.host", "localhost"))
      .put("port", parseInt(System.getProperty("binlog.port", "3306")))
      .put("schema", System.getProperty("binlog.schema"));
    config.put("url",
      "jdbc:mysql://" + config.getString("host") + ":" + config.getInteger("port") + "/" + config.getString("schema"));
    return config;
  }

  protected void insert() {
    rows.forEach(e ->
      executeSql("INSERT INTO `binlog_client_test` (id, name) " +
        " VALUES (" + e.getKey() + ", '" + e.getValue() + "');")
    );
  }

  protected void delete() {
    rows.forEach(e ->
      executeSql("DELETE FROM `binlog_client_test`" +
        " WHERE id=" + e.getKey())
    );
  }

  protected void update() {
    rows.forEach(e ->
      executeSql("UPDATE `binlog_client_test`" +
        " SET name='" + e.getValue() + "_updated" + "' WHERE id=" + e.getKey())
    );
  }

  protected List<Map.Entry<Integer, String>> rows() {
    return rows;
  }

  protected Integer lastId() {
    return rows.get(rows.size() - 1).getKey();
  }

  protected void executeSql(String sql) {
    logger.info("SQL: " + sql);
    try {
      conn
        .createStatement()
        .execute(sql);
    } catch (SQLException e) {
      fail(e);
    }
  }

}
