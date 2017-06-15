package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class EventBusTest extends BinlogClientTestBase {

  private MessageConsumer<JsonObject> consumer;

  @Test
  public void testInsert() throws SQLException {
    AtomicInteger counter = new AtomicInteger(0);
    consumer = vertx.eventBus().consumer(client.address(), (msg) -> {
      if (!"write".equals(msg.headers().get("type"))) {
        return;
      }
      JsonObject body = msg.body();
      logger.info(body.toString());
      assertEquals(config().getString("schema"), msg.headers().get("schema"));
      assertEquals("binlog_client_test", body.getString("table"));
      JsonObject row = body.getJsonObject("row");
      Integer id = row.getInteger("id");
      String name = row.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id.equals(lastId())) {
        consumer.unregister(onSuccess((ignore) -> testComplete()));
      }
    });
    insert();
    await();
  }

  @Test
  public void testDelete() throws SQLException {
    insert();
    AtomicInteger counter = new AtomicInteger(0);
    consumer = vertx.eventBus().consumer(client.address(), (msg) -> {
      if (!"delete".equals(msg.headers().get("type"))) {
        return;
      }
      JsonObject body = msg.body();
      logger.info(body.toString());
      assertEquals(config().getString("schema"), msg.headers().get("schema"));
      assertEquals("binlog_client_test", body.getString("table"));
      JsonObject row = body.getJsonObject("row");
      Integer id = row.getInteger("id");
      String name = row.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id.equals(lastId())) {
        consumer.unregister(onSuccess((ignore) -> testComplete()));
      }
    });
    delete();
    await();
  }

  @Test
  public void testUpdate() throws SQLException {
    insert();
    AtomicInteger counter = new AtomicInteger(0);
    consumer = vertx.eventBus().consumer(client.address(), (msg) -> {
      if (!"update".equals(msg.headers().get("type"))) {
        return;
      }
      JsonObject body = msg.body();
      logger.info(body.toString());
      assertEquals(config().getString("schema"), msg.headers().get("schema"));
      assertEquals("binlog_client_test", body.getString("table"));
      JsonObject row = body.getJsonObject("row");
      Integer id = row.getInteger("id");
      String name = row.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue() + "_updated", name);
      if (id.equals(lastId())) {
        consumer.unregister(onSuccess((ignore) -> testComplete()));
      }
    });
    update();
    await();
  }

}
