package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class PositionTest extends BinlogClientTestBase {

  @Test
  public void testReplay() {
    String filename = client.filename();
    long position = client.position();
    client.handler((event) -> {
      if (!"write".equals(event.getString("type"))) {
        return;
      }
      if (event.getJsonObject("row").getInteger("id").equals(lastId())) {
        replay(filename, position);
      }
    });
    insert();
    await();
  }

  private void replay(String filename, long position) {
    BinlogClient newClient = BinlogClient.create(vertx,
      new BinlogClientOptions()
        .setUsername(config().getString("user"))
        .setPassword(config().getString("password"))
        .setHost(config().getString("host"))
        .setPort(config().getInteger("port"))
        .setFilename(filename)
        .setPosition(position)
    );
    AtomicInteger counter = new AtomicInteger(0);
    newClient.connect(onSuccess((ar) -> {
      newClient.handler((event) -> {
        logger.info(event.toString());
        assertEquals(config().getString("schema"), event.getString("schema"));
        assertEquals("binlog_client_test", event.getString("table"));
        assertEquals("write", event.getString("type"));
        JsonObject row = event.getJsonObject("row");
        Integer id = row.getInteger("id");
        String name = row.getString("name");
        Map.Entry<Integer, String> expectedRow = rows().get(counter.getAndIncrement());
        assertEquals(expectedRow.getKey(), id);
        assertEquals(expectedRow.getValue(), name);
        if (id.equals(lastId())) {
          testComplete();
        }
      });
    }));
  }

}
