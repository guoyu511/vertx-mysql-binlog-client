package io.vertx.rxjava.ext.binlog.mysql;

import org.junit.Test;

import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.streams.Pump;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RxEventBusTest extends RxBinlogClientTestBase {

  @Test
  public void testSendStream() {
    Pump.pump(rxClient, rxVertx.eventBus().sender("binlog")).start();
    rxVertx.eventBus().consumer("binlog")
      .toObservable()
      .map(Message::body)
      .skip(rows().size() - 1)
      .subscribe((v) -> testComplete());
    insert();
    await();
  }

  @Test
  public void testPublishStream() {
    Pump.pump(rxClient, rxVertx.eventBus().publisher("binlog")).start();
    rxVertx.eventBus().consumer("binlog")
      .toObservable()
      .map(Message::body)
      .skip(rows().size() - 1)
      .subscribe((v) -> testComplete());
    insert();
    await();
  }

}
