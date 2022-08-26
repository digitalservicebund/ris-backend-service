package de.bund.digitalservice.ris.checkdocx;

import de.bund.digitalservice.ris.domain.docx.ErrorElement;
import de.bund.digitalservice.ris.domain.docx.ParagraphElement;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class CheckDocxConvertedPane extends JPanel implements IView {
  private final CheckDocxController controller;

  private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
  private final DefaultTreeModel contentTreeModel;

  public CheckDocxConvertedPane(CheckDocxController controller) {
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

    gbc.weighty = 0.0f;
    JCheckBox errorOnlyCheckBox = new JCheckBox("only error elements");
    errorOnlyCheckBox.addActionListener(
        e -> controller.setErrorsOnly(errorOnlyCheckBox.isSelected()));
    add(errorOnlyCheckBox, gbc);

    gbc.gridy = 1;
    gbc.weighty = 1.0f;
    contentTreeModel = new DefaultTreeModel(rootNode);
    JTree contentTree = new JTree(contentTreeModel);
    JScrollPane contentScrollPane = new JScrollPane(contentTree);
    add(contentScrollPane, gbc);
  }

  @Override
  public void update(NotificationType type) {
    if ((type == NotificationType.SELECT_FILE && controller.getSelectedTab() == this)
        || type == NotificationType.SELECT_CONVERTED_TAB) {
      rootNode.removeAllChildren();
      controller
          .getConvertedFileContent()
          .forEach(
              el -> {
                if (el instanceof ParagraphElement paragraphElement) {
                  DefaultMutableTreeNode node = new DefaultMutableTreeNode("paragraph");
                  paragraphElement
                      .getRunElements()
                      .forEach(
                          runElement ->
                              node.add(
                                  new DefaultMutableTreeNode(
                                      "run element: " + runElement.toString())));
                  rootNode.add(node);
                } else {
                  rootNode.add(new DefaultMutableTreeNode(el.getClass().getSimpleName()));
                }
              });
      contentTreeModel.reload();
    } else if (type == NotificationType.TOOGLE_ONLY_ERROR) {
      rootNode.removeAllChildren();
      controller
          .getConvertedFileContent()
          .forEach(
              el -> {
                if (el instanceof ParagraphElement paragraphElement) {
                  DefaultMutableTreeNode node = new DefaultMutableTreeNode("paragraph");
                  AtomicInteger i = new AtomicInteger(0);
                  paragraphElement
                      .getRunElements()
                      .forEach(
                          runElement -> {
                            i.incrementAndGet();
                            if (!controller.isErrorsOnly() || runElement instanceof ErrorElement) {
                              node.add(
                                  new DefaultMutableTreeNode(
                                      "run element(" + i.get() + "): " + runElement));
                            }
                          });
                  rootNode.add(node);
                } else {
                  rootNode.add(new DefaultMutableTreeNode(el.getClass().getSimpleName()));
                }
              });
      contentTreeModel.reload();
    }
  }
}
