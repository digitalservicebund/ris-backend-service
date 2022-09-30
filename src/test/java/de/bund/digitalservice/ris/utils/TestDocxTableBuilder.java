package de.bund.digitalservice.ris.utils;

import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

public class TestDocxTableBuilder {
  private List<Tr> rows = new ArrayList<>();
  private Tr activeRow;

  private TestDocxTableBuilder() {}

  public static TestDocxTableBuilder newInstance() {
    return new TestDocxTableBuilder();
  }

  public TestDocxTableBuilder addCell(Tc cell) {
    activeRow.getContent().add(new JAXBElement<>(new QName("tc"), Tc.class, cell));
    return this;
  }

  public TestDocxTableBuilder nextRow() {
    rows.add(activeRow);
    activeRow = new Tr();
    return this;
  }

  public Tbl build() {
    rows.add(activeRow);
    Tbl tbl = new Tbl();
    tbl.getContent().addAll(rows);
    return tbl;
  }
}
