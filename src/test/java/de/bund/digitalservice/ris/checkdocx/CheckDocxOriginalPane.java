package de.bund.digitalservice.ris.checkdocx;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Style;

public class CheckDocxOriginalPane extends JPanel implements IView {
  private final CheckDocxController controller;
  private final DocxElementList listModel;
  private final JTextArea elementInformation;
  private final JList<Object> contentList;

  public CheckDocxOriginalPane(CheckDocxController controller) {
    this.controller = controller;
    controller.addView(this);

    setLayout(new GridBagLayout());

    GridBagConstraints gbc =
        new GridBagConstraints(
            0,
            0,
            1,
            1,
            1.0f,
            1.0f,
            GridBagConstraints.LINE_START,
            GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5),
            5,
            5);

    listModel = new DocxElementList();
    contentList = new JList<>(listModel);
    contentList.setCellRenderer(new DocxElementCellRenderer());
    contentList.addListSelectionListener(
        l -> {
          if (l.getValueIsAdjusting()) {
            controller.setSelectedElement(contentList.getSelectedValue());
          }
        });
    JScrollPane scrollContentPane = new JScrollPane(contentList);
    add(scrollContentPane, gbc);

    gbc.gridy = 1;
    elementInformation = new JTextArea("test");
    add(elementInformation, gbc);
  }

  @Override
  public void update(NotificationType type) {
    if ((type == NotificationType.SELECT_FILE && controller.getSelectedTab() == this)
        || type == NotificationType.SELECT_ORIGINAL_TAB) {
      listModel.clear();
      controller.getOriginalFileContent().forEach(listModel::addElement);
      contentList.updateUI();
    } else if (type == NotificationType.SELECT_ELEMENT) {
      Object element = controller.getSelectedElement();
      elementInformation.setText("");
      if (element != null) {
        if (element instanceof P paragraph) {
          showInformationOfParagraph(paragraph);
        }
      }
    }
  }

  private void showInformationOfParagraph(P paragraph) {
    if (paragraph.getPPr() != null) {
      PPr pPr = paragraph.getPPr();
      if (pPr.getJc() != null) {
        elementInformation.append("jc: " + pPr.getJc().getVal() + "\n");
      }
      if (pPr.getPStyle() != null) {
        elementInformation.append("pStyle: " + pPr.getPStyle().getVal() + "\n");
        Style style = controller.getStyleInformation(pPr.getPStyle().getVal());
        elementInformation.append("  jc: " + style.getPPr().getJc().getVal() + "\n");
      }
    }
  }
}
