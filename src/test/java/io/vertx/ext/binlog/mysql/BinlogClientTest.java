package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class BinlogClientTest extends BinlogClientTestBase {

  @Test
  public void testInsert() throws SQLException {
    AtomicInteger counter = new AtomicInteger(0);
    client.handler((event) -> {
      if (!"write".equals(event.getString("type"))) {
        return;
      }
      logger.info(event.toString());
      assertEquals(config().getString("schema"), event.getString("schema"));
      assertEquals("binlog_client_test", event.getString("table"));
      JsonObject json = event.getJsonObject("row");
      Integer id = json.getInteger("id");
      String name = json.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id.equals(lastId())) {
        testComplete();
      }
    });
    insert();
    await();
  }

  @Test
  public void testDelete() throws SQLException {
    insert();
    AtomicInteger counter = new AtomicInteger(0);
    client.handler((event) -> {
      if (!"delete".equals(event.getString("type"))) {
        return;
      }
      logger.info(event.toString());
      assertEquals(config().getString("schema"), event.getString("schema"));
      assertEquals("binlog_client_test", event.getString("table"));
      assertEquals("delete", event.getString("type"));
      JsonObject json = event.getJsonObject("row");
      Integer id = json.getInteger("id");
      String name = json.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id.equals(lastId())) {
        testComplete();
      }
    });
    delete();
    await();
  }

  @Test
  public void testUpdate() throws SQLException {
    insert();
    AtomicInteger counter = new AtomicInteger(0);
    client.handler((event) -> {
      if (!"update".equals(event.getString("type"))) {
        return;
      }
      logger.info(event.toString());
      assertEquals(config().getString("schema"), event.getString("schema"));
      assertEquals("binlog_client_test", event.getString("table"));
      assertEquals("update", event.getString("type"));
      JsonObject json = event.getJsonObject("row");
      Integer id = json.getInteger("id");
      String name = json.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue() + "_updated", name);
      if (id.equals(lastId())) {
        testComplete();
      }
    });
    update();
    await();
  }

}
