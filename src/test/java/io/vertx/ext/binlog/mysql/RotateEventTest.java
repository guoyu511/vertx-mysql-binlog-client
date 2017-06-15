package io.vertx.ext.binlog.mysql;

import org.junit.Test;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RotateEventTest extends BinlogClientTestBase {

  private Logger logger = LoggerFactory.getLogger(RotateEventTest.class);

  @Test
  public void testRotateEvent() {
    client.handler((event) -> {
      if ("rotate".equals(event.getString("type"))) {
        logger.info(event);
        assertNotNull(event.getString("filename"));
        assertNotNull(event.getLong("position"));
        testComplete();
      }
    });
    await();
  }

}
