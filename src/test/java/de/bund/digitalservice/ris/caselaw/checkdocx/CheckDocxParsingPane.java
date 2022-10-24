package de.bund.digitalservice.ris.caselaw.checkdocx;

import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckDocxParsingPane extends JTabbedPane implements IView {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckDocxParsingPane.class);

  private final CheckDocxController controller;

  public CheckDocxParsingPane(CheckDocxController controller) {
    this.controller = controller;
    controller.addView(this);

    addTab("converted file", new CheckDocxConvertedPane(controller));
    addTab("original file", new CheckDocxOriginalPane(controller));
    addTab("xml of file", new CheckDocxXMLPane(controller));

    addChangeListener(l -> controller.changeTab(getSelectedComponent()));
  }

  @Override
  public void update(NotificationType type) {}
}
