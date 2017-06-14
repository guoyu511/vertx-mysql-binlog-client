package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class StartStopTest extends BinlogClientTestBase {

  @Test
  public void testStartAndStop() {
    List<JsonObject> eventReceived = new ArrayList<>();
    client.handler(eventReceived::add);
    client.stop();
    insert();
    vertx.setTimer(5000, (v) -> {
      assertEquals(eventReceived.size(), 0);
      client.handler((evt) -> {
        if ("write".equals(evt.getString("type"))) {
          eventReceived.add(evt);
        }
        if (eventReceived.size() == rows().size()) {
          testComplete();
        }
      });
      client.start();
    });
    await();
  }


}
