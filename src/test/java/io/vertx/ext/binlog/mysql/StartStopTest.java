package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.binlog.mysql.impl.BinlogClientImpl;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class StartStopTest extends BinlogClientTestBase {

  @Test
  public void testStartAndStop() {
    List<JsonObject> eventReceived = new ArrayList<>();
    client.handler(eventReceived::add);
    client.stop((ar) -> {
      assertFalse(((BinlogClientImpl)client).getClinet().isConnected());
    });
    insert();
    vertx.setTimer(1000, (v) -> {
      assertEquals(eventReceived.size(), 0);
      client.handler((evt) -> {
        if ("write".equals(evt.getString("type"))) {
          eventReceived.add(evt);
        }
        if (eventReceived.size() == rows().size()) {
          testComplete();
        }
      });
      client.start((ar) -> {
        assertTrue(((BinlogClientImpl)client).getClinet().isConnected());
      });
    });
    await();
  }


}
