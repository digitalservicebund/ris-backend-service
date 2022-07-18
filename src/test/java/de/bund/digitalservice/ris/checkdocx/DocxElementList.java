package de.bund.digitalservice.ris.checkdocx;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

public class DocxElementList implements ListModel<Object> {
  private final List<Object> elements = new ArrayList<>();

  public void addElement(Object element) {
    elements.add(element);
    if (element instanceof P paragraph) {
      paragraph
          .getContent()
          .forEach(
              el -> {
                elements.add(el);
                if (el instanceof R run) {
                  elements.addAll(run.getContent());
                }
              });
    }
  }

  @Override
  public int getSize() {
    return elements.size();
  }

  @Override
  public Object getElementAt(int index) {
    return elements.get(index);
  }

  @Override
  public void addListDataListener(ListDataListener l) {}

  @Override
  public void removeListDataListener(ListDataListener l) {}

  public void clear() {
    elements.clear();
  }
}
