package io.vertx.rxjava.ext.binlog.mysql;


import io.vertx.ext.binlog.mysql.BinlogClientTestBase;
import io.vertx.rxjava.core.Vertx;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RxBinlogClientTestBase extends BinlogClientTestBase {

  protected Vertx rxVertx;

  protected BinlogClient rxClient;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    rxVertx = Vertx.newInstance(vertx);
    rxClient = BinlogClient.newInstance(client);
  }

}
