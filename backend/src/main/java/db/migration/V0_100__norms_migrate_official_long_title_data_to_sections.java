package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_100__norms_migrate_official_long_title_data_to_sections extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {

    final Connection connection = context.getConnection();

    // GUIDs and long titles of norms without NORM section
    final List<UUID> noNormSectionNorms = new ArrayList<>();
    try (final Statement stmt = connection.createStatement()) {
      try (final ResultSet result =
          stmt.executeQuery(
              "SELECT guid FROM norms WHERE guid NOT IN (SELECT norm_guid FROM metadata_sections WHERE name = 'NORM')")) {
        while (result.next()) {
          noNormSectionNorms.add(result.getObject("guid", UUID.class));
        }
      }
    }

    // GUIDs and long titles of norms with NORM section but without OFFICIAL_LONG_TITLE
    final List<UUID> normsWithNormSectionButNotLongTitle = new ArrayList<>();
    try (final Statement stmt = connection.createStatement()) {
      try (final ResultSet result =
          stmt.executeQuery(
              "select guid "
                  + "from norms n "
                  + "where guid in (select ms.norm_guid "
                  + "from metadata_sections ms "
                  + "where ms.name = 'NORM' "
                  + "and ms.guid not in (select section_guid "
                  + "from metadata "
                  + "where type = 'OFFICIAL_LONG_TITLE'))")) {
        while (result.next()) {
          normsWithNormSectionButNotLongTitle.add(result.getObject("guid", UUID.class));
        }
      }
    }

    try (final Statement add = connection.createStatement()) {
      try (final ResultSet guidAndOfficialLongTitlesResult =
          add.executeQuery("SELECT guid, official_long_title FROM norms")) {
        while (guidAndOfficialLongTitlesResult.next()) {
          final UUID normGuid = guidAndOfficialLongTitlesResult.getObject("guid", UUID.class);
          final String officialLongTitle =
              guidAndOfficialLongTitlesResult.getString("official_long_title");

          // a. Get new sectionGUID (create new NORM section)
          UUID sectionGUID = null;
          if (noNormSectionNorms.contains(normGuid)) {
            try (final PreparedStatement change =
                connection.prepareStatement(
                    "INSERT INTO metadata_sections (name, order_number, guid, section_guid, norm_guid) "
                        + "VALUES ('NORM', 1, ?, NULL, ?)")) {
              sectionGUID = UUID.randomUUID();
              change.setObject(1, sectionGUID);
              change.setObject(2, normGuid);
              change.execute();
            }

            // b. Get sectionGUID of already existing NORM section
          } else if (normsWithNormSectionButNotLongTitle.contains(normGuid)) {
            try (final PreparedStatement findParent =
                connection.prepareStatement(
                    "SELECT guid FROM metadata_sections WHERE norm_guid=? and name='NORM'")) {
              findParent.setObject(1, normGuid);
              try (final ResultSet parentGuid = findParent.executeQuery()) {
                parentGuid.next();
                sectionGUID = parentGuid.getObject("guid", UUID.class);
              }
            }
          }

          // Create metadatum
          if (sectionGUID != null && officialLongTitle != null) {
            try (final PreparedStatement change =
                connection.prepareStatement(
                    "INSERT INTO metadata (value, type, order_number, guid, section_guid) "
                        + "VALUES (?, 'OFFICIAL_LONG_TITLE', 1, ?, ?)")) {
              change.setString(1, officialLongTitle);
              change.setObject(2, UUID.randomUUID());
              change.setObject(3, sectionGUID);
              change.execute();
            }
          }
        }
      }
    }
  }
}
