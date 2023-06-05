package db.migration;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_79__caselaw_insert_default_documentation_offices extends BaseJavaMigration {
  public void migrate(Context context) throws SQLException {
    try (PreparedStatement statement =
        context
            .getConnection()
            .prepareStatement(
                "INSERT INTO documentation_office (id, label, abbreviation) "
                    + "SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM documentation_office WHERE label=?)")) {

      insertDocumentationOffice(statement, "BGH", "KO");
      insertDocumentationOffice(statement, "BVerfG", "KV");
      insertDocumentationOffice(statement, "DigitalService", "XX");
      insertDocumentationOffice(statement, "CC-RIS", "XX");
      insertDocumentationOffice(statement, "BAG", "EF");
      insertDocumentationOffice(statement, "BFH", "ST");
      insertDocumentationOffice(statement, "BPatG", "MP");
      insertDocumentationOffice(statement, "BSG", "KS");
      insertDocumentationOffice(statement, "BVerwG", "LE");
      insertDocumentationOffice(statement, "OVG NRW", "MW");
      insertDocumentationOffice(statement, "BZSt", "FM");
    }
  }

  private void insertDocumentationOffice(
      PreparedStatement statement, String label, String abbreviation) throws SQLException {
    statement.setObject(1, UUID.randomUUID());
    statement.setString(2, label);
    statement.setString(3, abbreviation);
    statement.setString(4, label);
    statement.execute();
  }
}
