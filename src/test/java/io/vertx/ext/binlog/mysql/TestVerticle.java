package io.vertx.ext.binlog.mysql;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class TestVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    BinlogClient client = BinlogClient.create(vertx,
      new BinlogClientOptions()
        .setHost("l-fedev2.ops.bj0.daling.com")
        .setPort(3306)
        .setSchema("fe_dev")
        .setUsername("fe")
        .setPassword("fedev"));
    client.start(startFuture);
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(TestVerticle.class.getName());
  }

}
