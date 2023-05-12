package db.migration;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_62__update_guid_of_sections extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {
    try (Statement add = context.getConnection().createStatement()) {
      try (ResultSet rows =
          add.executeQuery("SELECT id,guid FROM metadata_sections WHERE guid IS NULL")) {
        while (rows.next()) {
          int id = rows.getInt(1);
          UUID guid = UUID.randomUUID();
          try (Statement update = context.getConnection().createStatement()) {
            update.execute("UPDATE metadata_sections SET guid='" + guid + "' WHERE id=" + id);
          }
        }
      }

      try (Statement change = context.getConnection().createStatement()) {
        change.execute("ALTER TABLE metadata_sections ALTER COLUMN guid SET NOT NULL");
      }
    }
  }
}
