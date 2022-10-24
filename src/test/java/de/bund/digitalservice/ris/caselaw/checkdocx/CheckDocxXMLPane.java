package de.bund.digitalservice.ris.caselaw.checkdocx;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class CheckDocxXMLPane extends JPanel implements IView {
  private final CheckDocxController controller;
  private final JTextArea elementInformation;

  public CheckDocxXMLPane(CheckDocxController controller) {
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

    gbc.weightx = 0.8f;
    gbc.weighty = 0.0f;
    JTextField xpathField = new JTextField();
    add(xpathField, gbc);

    gbc.weightx = 0.2f;
    gbc.gridx = 1;
    JButton xpathFilterButton = new JButton("filter");
    xpathFilterButton.addActionListener(l -> controller.filterXML(xpathField.getText()));
    add(xpathFilterButton, gbc);

    gbc.gridwidth = 2;
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1.0f;
    gbc.weighty = 1.0f;
    elementInformation = new JTextArea("test");
    JScrollPane scrollXmlPane = new JScrollPane(elementInformation);
    add(scrollXmlPane, gbc);
  }

  @Override
  public void update(NotificationType type) {
    if ((type == NotificationType.SELECT_FILE && controller.getSelectedTab() == this)
        || type == NotificationType.SELECT_XML_TAB
        || type == NotificationType.FILTER_XML) {

      elementInformation.setText(controller.getXml());
    }
  }
}
