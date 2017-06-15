package io.vertx.ext.binlog.mysql;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class AlterTableTest extends BinlogClientTestBase {

  @Test
  @Ignore
  public void testAddColumn() {
    String extraValue = UUID.randomUUID().toString();
    String randomName = UUID.randomUUID().toString();
    client.handler((event) -> {
      if (!"write".equals(event.getString("type"))) {
        return;
      }
      JsonObject row = event.getJsonObject("row");
      assertEquals(randomName, row.getString("name"));
      assertEquals(extraValue, row.getString("extra"));
      testComplete();
    });
    executeSql("ALTER TABLE `binlog_client_test` ADD COLUMN extra varchar(100);");
    executeSql("INSERT INTO `binlog_client_test` (id, name, extra)" +
      " values (" +  (lastId() + 1) + "," +
      " '" + randomName  + "'," +
      " '" + extraValue + "')");
    await();
  }

  @Test
  public void testDropColumn() {
    client.handler((event) -> {
      if (!"write".equals(event.getString("type"))) {
        return;
      }
      JsonObject row = event.getJsonObject("row");
      assertFalse(row.containsKey("name"));
      testComplete();
    });
    executeSql("ALTER TABLE `binlog_client_test` DROP COLUMN name;");
    executeSql("INSERT INTO `binlog_client_test` (id)" +
      " values (" +  (lastId() + 1) + ")");
    await();
  }

}
