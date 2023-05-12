package db.migration;

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
          int childSectionId = rows.findColumn("id");
          int normId = rows.findColumn("norm_id");
          int order = rows.findColumn("order_number");

          UUID parentGuid = UUID.randomUUID();

          try (Statement change = context.getConnection().createStatement()) {
            change.execute(
                "INSERT INTO metadata_sections (name, norm_id, section_id, order_number, guid) VALUES ('OFFICIAL_REFERENCE', "
                    + normId
                    + ", NULL, "
                    + order
                    + ", '"
                    + parentGuid
                    + "');");
          }

          try (Statement findParent = context.getConnection().createStatement()) {
            try (ResultSet parent =
                findParent.executeQuery(
                    "SELECT * FROM metadata_sections WHERE guid=" + parentGuid)) {
              parent.next();
              try (Statement update = context.getConnection().createStatement()) {
                update.execute(
                    "UPDATE metadata_sections SET section_id='"
                        + parentGuid
                        + "', order_number=1 WHERE id="
                        + childSectionId);
              }
            }
          }
        }
      }
    }
  }
}
