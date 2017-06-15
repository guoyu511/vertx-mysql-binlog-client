package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class PauseResumeTest extends BinlogClientTestBase {

  @Test
  public void testPauseAndResume() {
    List<JsonObject> eventReceived = new ArrayList<>();
    client.handler(eventReceived::add);
    client.pause();
    insert();
    vertx.setTimer(5000, (v) -> {
      assertEquals(eventReceived.size(), 0);
      client.handler((event) -> {
        if ("write".equals(event.getString("type"))) {
          logger.info(event.toString());
          eventReceived.add(event);
        }
        if (eventReceived.size() == rows().size()) {
          testComplete();
        }
      });
      client.resume();
    });
    await();
  }


}
