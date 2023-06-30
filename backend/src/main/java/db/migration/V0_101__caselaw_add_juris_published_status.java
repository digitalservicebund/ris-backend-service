package db.migration;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_101__caselaw_add_juris_published_status extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {
    final Connection connection = context.getConnection();

    try (final Statement stmt = connection.createStatement()) {
      stmt.execute(
          "INSERT INTO document_unit_status "
              + "SELECT "
              + UUID.randomUUID()
              + ", now(), 'JURIS_PUBLISHED', du.uuid, null "
              + "FROM doc_unit du "
              + "LEFT JOIN document_unit_status dus on du.uuid = dus.document_unit_id "
              + "WHERE dus.status IS NULL");
    }
  }
}
