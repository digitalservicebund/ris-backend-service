package de.bund.digitalservice.ris.caselaw.checkdocx;

import jakarta.xml.bind.JAXBElement;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class DocxElementCellRenderer implements ListCellRenderer<Object> {
  @Override
  public Component getListCellRendererComponent(
      JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

    String text = value.toString();

    if (value instanceof JAXBElement<?> jaxbElement) {
      text = jaxbElement.getDeclaredType().getSimpleName();
    }

    JLabel label = new JLabel(text);

    if (isSelected) {
      System.err.println("isSelected");
      label.setBackground(Color.CYAN);
    }

    return label;
  }
}
