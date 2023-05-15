package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_63__update_sections_for_official_reference extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {
    try (Statement add = context.getConnection().createStatement()) {
      try (ResultSet rows =
          add.executeQuery(
              "SELECT * FROM metadata_sections WHERE name IN ('PRINT_ANNOUNCEMENT', 'DIGITAL_ANNOUNCEMENT', 'EU_ANNOUNCEMENT', 'OTHER_OFFICIAL_ANNOUNCEMENT') AND section_id IS NULL")) {
        while (rows.next()) {
          int childSectionId = rows.getInt("id");
          int normId = rows.getInt("norm_id");
          int order = rows.getInt("order_number");

          UUID parentGuid = UUID.randomUUID();

          try (PreparedStatement change =
              context
                  .getConnection()
                  .prepareStatement(
                      "INSERT INTO metadata_sections (name, norm_id, section_id, order_number, guid) VALUES ('OFFICIAL_REFERENCE', ?, NULL, ?, ?);")) {
            change.setInt(1, normId);
            change.setInt(2, order);
            change.setObject(3, parentGuid);
            change.execute();
          }

          try (PreparedStatement findParent =
              context
                  .getConnection()
                  .prepareStatement("SELECT * FROM metadata_sections WHERE guid=?")) {
            findParent.setObject(1, parentGuid);
            try (ResultSet parent = findParent.executeQuery()) {
              parent.next();
              try (PreparedStatement update =
                  context
                      .getConnection()
                      .prepareStatement(
                          "UPDATE metadata_sections SET section_id=?, order_number=1 WHERE id=?")) {
                update.setInt(1, parent.getInt("id"));
                update.setInt(2, childSectionId);
                update.execute();
              }
            }
          }
        }
      }
    }
  }
}
