package io.vertx.rxjava.ext.binlog.mysql;

import org.junit.Test;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RxObservableTest extends RxBinlogClientTestBase {

  @Test
  public void testInsertRxStream() {
    rxClient.toObservable()
      .filter((event) -> "write".equals(event.getString("type")))
      .skip(rows().size() - 1)
      .subscribe((v) -> testComplete());
    insert();
    await();
  }

  @Test
  public void testUpdateRxStream() {
    insert();
    rxClient.toObservable()
      .filter((event) -> "update".equals(event.getString("type")))
      .skip(rows().size() - 1)
      .subscribe((v) -> testComplete());
    update();
    await();
  }

  @Test
  public void testDeleteRxStream() {
    insert();
    rxClient.toObservable()
      .filter((event) -> "delete".equals(event.getString("type")))
      .skip(rows().size() - 1)
      .subscribe((v) -> testComplete());
    delete();
    await();
  }

}
