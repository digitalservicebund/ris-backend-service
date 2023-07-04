package db.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_102__norms_migrate_announcement_date_data_to_sections extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {
    final Connection connection = context.getConnection();
    final HashMap<UUID, String> normGuidToFlatAnnouncementDate =
        this.getNormGuidToFlatAnnouncementDateForNormsWithoutAnnoucementDateSection(connection);

    for (Map.Entry<UUID, String> entry : normGuidToFlatAnnouncementDate.entrySet()) {
      final UUID normGuid = entry.getKey();
      final String announcementDate = entry.getValue();
      final UUID sectionGuid = this.insertAnnouncementDateSection(connection, normGuid);
      insertAnnouncementDateMetadatum(connection, sectionGuid, announcementDate);
    }
  }

  private HashMap<UUID, String>
      getNormGuidToFlatAnnouncementDateForNormsWithoutAnnoucementDateSection(
          final Connection connection) throws Exception {
    HashMap<UUID, String> normGuidToFlatAnnouncementDate = new HashMap();

    final String query =
        """
        SELECT guid, announcement_date
        FROM norms
        WHERE guid NOT IN
            (SELECT norm_guid
              FROM metadata_sections
              WHERE name = 'ANNOUNCEMENT_DATE')
          AND announcement_date IS NOT NULL
        """;

    try (final Statement statement = connection.createStatement()) {
      try (final ResultSet result = statement.executeQuery(query)) {
        while (result.next()) {
          final UUID guid = result.getObject("guid", UUID.class);
          final String announcementDate = result.getString("announcement_date");
          normGuidToFlatAnnouncementDate.put(guid, announcementDate);
        }
      }
    }

    return normGuidToFlatAnnouncementDate;
  }

  private UUID insertAnnouncementDateSection(final Connection connection, final UUID normGuid)
      throws Exception {
    final UUID sectionGuid = UUID.randomUUID();
    final String query =
        String.format(
            """
        INSERT INTO metadata_sections (name, order_number, guid, section_guid, norm_guid)
        VALUES ('ANNOUNCEMENT_DATE', 1, '%s', NULL, '%s')
    """,
            sectionGuid, normGuid);

    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(query);
    }

    return sectionGuid;
  }

  private void insertAnnouncementDateMetadatum(
      final Connection connection, final UUID sectionGuid, final String announcementDate)
      throws Exception {
    final UUID metadataGuid = UUID.randomUUID();
    final String query =
        String.format(
            """
      INSERT INTO metadata (value, type, order_number, guid, section_guid)
      VALUES ('%s', 'DATE', 1, '%s', '%s')
    """,
            announcementDate, metadataGuid, sectionGuid);

    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(query);
    }
  }
}
