package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.config.ConverterConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import tools.jackson.databind.json.JsonMapper;

public class DocumentationUnitType implements UserType<DocumentationUnit> {
  private final JsonMapper mapper = new ConverterConfig().jsonMapper();

  @Override
  public int getSqlType() {
    return SqlTypes.JSON;
  }

  @Override
  public Class<DocumentationUnit> returnedClass() {
    return DocumentationUnit.class;
  }

  @Override
  public DocumentationUnit nullSafeGet(ResultSet rs, int position, WrapperOptions options)
      throws SQLException {

    String cellContent = rs.getString(position);

    if (cellContent != null) {
      return mapper.readValue(cellContent, DocumentationUnit.class);
    }

    return null;
  }

  @Override
  public void nullSafeSet(
      PreparedStatement st, DocumentationUnit value, int position, WrapperOptions options)
      throws SQLException {

    if (value == null) {
      st.setNull(position, Types.OTHER);
      return;
    }

    final StringWriter w = new StringWriter();
    mapper.writeValue(w, value);
    w.flush();
    st.setObject(position, w.toString(), Types.OTHER);
  }

  @Override
  public DocumentationUnit deepCopy(DocumentationUnit documentationUnit) {
    final StringWriter w = new StringWriter();
    mapper.writeValue(w, documentationUnit);
    w.flush();

    return mapper.readValue(w.toString(), DocumentationUnit.class);
  }

  @Override
  public boolean isMutable() {
    return false;
  }
}
