package de.bund.digitalservice.ris.caselaw.checkdocx;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CheckDocxContentPane extends JPanel implements IView {
  private final DefaultListModel<String> fileListModel;
  private final CheckDocxController controller;

  public CheckDocxContentPane(CheckDocxController controller) {
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

    fileListModel = new DefaultListModel<>();
    JList<String> fileList = new JList<>(fileListModel);
    fileList.addListSelectionListener(
        l -> {
          if (l.getValueIsAdjusting()) {
            controller.readDocx(fileList.getSelectedValue());
          }
        });
    JScrollPane fileScrollPane = new JScrollPane(fileList);
    add(fileScrollPane, gbc);

    gbc.gridx = 1;
    CheckDocxParsingPane parsingPane = new CheckDocxParsingPane(controller);
    add(parsingPane, gbc);
  }

  @Override
  public void update(NotificationType type) {
    if (type == NotificationType.READ_DIRECTORY) {
      fileListModel.clear();
      fileListModel.addAll(controller.getFiles());
    }
  }
}
