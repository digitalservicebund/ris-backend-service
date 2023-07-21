package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_110__caselaw_add_uuid_to_document_unit_norm extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {
    try (Statement add = context.getConnection().createStatement()) {
      try (ResultSet rows =
          add.executeQuery("SELECT id,uuid FROM document_unit_norm WHERE uuid IS NULL")) {
        while (rows.next()) {
          int id = rows.getInt(1);
          UUID uuid = UUID.randomUUID();
          try (PreparedStatement preparedStatement =
              context
                  .getConnection()
                  .prepareStatement("UPDATE document_unit_norm SET uuid=? WHERE id=?")) {
            preparedStatement.setObject(1, uuid);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
          }
        }
      }

      try (Statement change = context.getConnection().createStatement()) {
        change.execute("ALTER TABLE document_unit_norm ALTER COLUMN uuid SET NOT NULL");
        change.execute(
            "ALTER TABLE document_unit_norm ADD CONSTRAINT norm_uuid_unique UNIQUE (uuid)");
      }
    }
  }
}
