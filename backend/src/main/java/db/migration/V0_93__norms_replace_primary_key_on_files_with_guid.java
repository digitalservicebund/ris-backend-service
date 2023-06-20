package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V0_93__norms_replace_primary_key_on_files_with_guid extends BaseJavaMigration {
  public void migrate(Context context) throws Exception {

    final Connection connection = context.getConnection();

    try (final Statement stmt = connection.createStatement()) {
      stmt.execute("ALTER TABLE files DROP CONSTRAINT files_pkey");
    }

    try (Statement stmt = connection.createStatement()) {
      stmt.execute("ALTER TABLE files ADD COLUMN guid uuid");
    }

    try (final Statement stmt = connection.createStatement()) {
      try (final ResultSet rows = stmt.executeQuery("SELECT id FROM files")) {
        while (rows.next()) {
          int id = rows.getInt(1);
          final UUID guid = UUID.randomUUID();
          try (final PreparedStatement preparedStatement =
              connection.prepareStatement("UPDATE files SET guid=? WHERE id=?")) {
            preparedStatement.setObject(1, guid);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
          }
        }
      }
    }

    try (Statement stmt = connection.createStatement()) {
      stmt.execute("ALTER TABLE files ADD PRIMARY KEY (guid)");
    }

    try (Statement stmt = connection.createStatement()) {
      stmt.execute("ALTER TABLE files DROP COLUMN id");
    }

    try (Statement stmt = connection.createStatement()) {
      stmt.execute("ALTER TABLE files DROP COLUMN norm_id");
    }
  }
}
