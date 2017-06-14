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
    client.handler((evt) -> {
      if ("rotate".equals(evt.getString("type"))) {
        logger.info("Handle rotate event " +
          evt.getString("filename") + "/" + evt.getLong("position"));
        assertNotNull(evt.getString("filename"));
        assertNotNull(evt.getLong("position"));
        testComplete();
      }
    });
    await();
  }

}
