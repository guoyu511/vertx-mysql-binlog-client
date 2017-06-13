package io.vertx.ext.binlog.mysql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class BinlogClientTest extends BinlogClientTestBase {

  private BinlogClient client;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    client = BinlogClient.create(vertx,
      new BinlogClientOptions()
        .setUsername(config().getString("user"))
        .setPassword(config().getString("password"))
        .setHost(config().getString("host"))
        .setPort(config().getInteger("port"))
        .setSchema(config().getString("schema"))
        .setHeartbeatInterval(5000)
    );
    CountDownLatch latch = new CountDownLatch(1);
    client.start(onSuccess((ignore) ->
      latch.countDown()
    ));
    awaitLatch(latch);
  }

  @After
  public void after() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client.stop((ignore) ->
      latch.countDown()
    );
    awaitLatch(latch);
    delete();
  }

  @Test
  public void testInsert() throws SQLException {
    AtomicInteger counter = new AtomicInteger(0);
    client.handler((event) -> {
      if (!"write".equals(event.type())) {
        return;
      }
      assertEquals(config().getString("schema"), event.schema());
      assertEquals("binlog_client_test", event.table());
      JsonObject json = event.body();
      Integer id = json.getInteger("id");
      String name = json.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id == 100) {
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
      if (!"delete".equals(event.type())) {
        return;
      }
      assertEquals(config().getString("schema"), event.schema());
      assertEquals("binlog_client_test", event.table());
      assertEquals("delete", event.type());
      JsonObject json = event.body();
      Integer id = json.getInteger("id");
      String name = json.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue(), name);
      if (id == 100) {
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
      if (!"update".equals(event.type())) {
        return;
      }
      assertEquals(config().getString("schema"), event.schema());
      assertEquals("binlog_client_test", event.table());
      assertEquals("update", event.type());
      JsonObject json = event.body();
      Integer id = json.getInteger("id");
      String name = json.getString("name");
      Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
      assertEquals(expectedRow.getKey(), id);
      assertEquals(expectedRow.getValue() + "_updated", name);
      if (id == 100) {
        testComplete();
      }
    });
    update();
    await();
  }

}
