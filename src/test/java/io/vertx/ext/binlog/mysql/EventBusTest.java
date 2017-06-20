package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class EventBusTest extends BinlogClientTestBase {

  @Test
  public void testInsert() throws SQLException {
    AtomicInteger counter = new AtomicInteger(0);
    Pump.pump(client, vertx.eventBus().publisher("binlog")).start();
    vertx.eventBus().<JsonObject>consumer("binlog", (msg) -> {
      JsonObject body = msg.body();
      if (!"write".equals(body.getString("type"))) {
        return;
      }
      assertEquals(config().getString("schema"), body.getString("schema"));
      assertEquals("binlog_client_test", body.getString("table"));
      JsonObject row = body.getJsonObject("row");
      Integer id = row.getInteger("id");
      String name = row.getString("name");
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
    AtomicInteger counter = new AtomicInteger(0);
    Pump.pump(client, vertx.eventBus().publisher("binlog")).start();
    vertx.eventBus().<JsonObject>consumer("binlog", (msg) -> {
      JsonObject body = msg.body();
      if (!"delete".equals(body.getString("type"))) {
        return;
      }
      assertEquals(config().getString("schema"), body.getString("schema"));
      assertEquals("binlog_client_test", body.getString("table"));
      JsonObject row = body.getJsonObject("row");
      Integer id = row.getInteger("id");
      String name = row.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id.equals(lastId())) {
        testComplete();
      }
    });
    insert();
    delete();
    await();
  }

  @Test
  public void testUpdate() throws SQLException {
    AtomicInteger counter = new AtomicInteger(0);
    Pump.pump(client, vertx.eventBus().publisher("binlog")).start();
    vertx.eventBus().<JsonObject>consumer("binlog", (msg) -> {
      JsonObject body = msg.body();
      if (!"update".equals(body.getString("type"))) {
        return;
      }
      assertEquals(config().getString("schema"), body.getString("schema"));
      assertEquals("binlog_client_test", body.getString("table"));
      JsonObject row = body.getJsonObject("row");
      Integer id = row.getInteger("id");
      String name = row.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue() + "_updated", name);
      if (id.equals(lastId())) {
        testComplete();
      }
    });
    insert();
    update();
    await();
  }

}
