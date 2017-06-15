package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.binlog.mysql.impl.BinlogClientImpl;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class CloseTest extends BinlogClientTestBase {

  @Test
  public void testClose() {
    List<JsonObject> eventReceived = new ArrayList<>();
    client.handler(eventReceived::add);
    client.close((ar) -> {
      assertFalse(((BinlogClientImpl)client).getClient().isConnected());
    });
    insert();
    vertx.setTimer(1000, (v) -> {
      assertEquals(eventReceived.size(), 0);
      testComplete();
    });
    await();
  }


}
