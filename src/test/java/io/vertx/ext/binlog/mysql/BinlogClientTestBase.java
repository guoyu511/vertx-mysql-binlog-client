package io.vertx.ext.binlog.mysql;


import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  @BeforeClass
  public static void init() throws Exception {
    conn = DriverManager.getConnection(
      config().getString("url"),
      config().getString("user"),
      config().getString("password")
    );
    for (String sql : SQL) {
      conn.createStatement().execute(sql);
    }
    rows = IntStream
      .iterate(1, i -> i + 1)
      .limit(100)
      .boxed()
      .map(i -> new AbstractMap.SimpleEntry<>(i, UUID.randomUUID().toString()))
      .collect(Collectors.toList());
  }

  static JsonObject config() {
    JsonObject config = new JsonObject()
      .put("user", System.getProperty("binlog.user", "root"))
      .put("password", System.getProperty("binlog.password", ""))
      .put("host", System.getProperty("binlog.host", "localhost"))
      .put("port", parseInt(System.getProperty("binlog.port", "3306")))
      .put("schema", System.getProperty("binlog.schema"));
    config.put("url",
      "jdbc:mysql://" + config.getString("host") + ":" + config.getInteger("port") + "/" + config.getString("schema"));
    return config;
  }

  void insert() {
    rows.forEach(e ->
      executeSql("INSERT INTO `binlog_client_test`  (id, name) " +
        " VALUES (" + e.getKey() + ", '" + e.getValue() + "');")
    );
  }

  void delete() {
    rows.forEach(e ->
      executeSql("DELETE FROM `binlog_client_test`" +
        " WHERE id=" + e.getKey())
    );
  }

  void update() {
    rows.forEach(e ->
      executeSql("UPDATE `binlog_client_test`" +
        " SET name='" + e.getValue() + "_updated" + "' WHERE id=" + e.getKey())
    );
  }

  List<Map.Entry<Integer, String>> rows() {
    return rows;
  }

  void executeSql(String sql) {
    try {
      conn
        .createStatement()
        .execute(sql);
    } catch (SQLException e) {
      fail(e);
    }
  }

}
