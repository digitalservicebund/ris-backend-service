package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_68__norms_add_guid_to_existing_metadata extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {
    try (Statement add = context.getConnection().createStatement()) {
      try (ResultSet rows = add.executeQuery("SELECT id,guid FROM metadata WHERE guid IS NULL")) {
        while (rows.next()) {
          int id = rows.getInt(1);
          UUID guid = UUID.randomUUID();
          try (PreparedStatement preparedStatement =
              context.getConnection().prepareStatement("UPDATE metadata SET guid=? WHERE id=?")) {
            preparedStatement.setObject(1, guid);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
          }
        }
      }

      try (Statement change = context.getConnection().createStatement()) {
        change.execute("ALTER TABLE metadata ALTER COLUMN guid SET NOT NULL");
      }
    }
  }
}
