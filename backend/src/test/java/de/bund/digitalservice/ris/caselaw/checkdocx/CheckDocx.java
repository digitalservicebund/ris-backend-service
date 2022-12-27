package de.bund.digitalservice.ris.caselaw.checkdocx;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class CheckDocx {
  public static void main(String[] args) {
    CheckDocxController controller = new CheckDocxController();
    CheckDocxContentPane contentPane = new CheckDocxContentPane(controller);

    File[] savedDirectory = {null};

    try {
      BufferedReader br = new BufferedReader(new FileReader(".config"));
      String savedDirectoryPath = br.readLine();
      if (savedDirectoryPath != null) {
        savedDirectory[0] = new File(savedDirectoryPath);
        if (!savedDirectory[0].exists() || !savedDirectory[0].isDirectory()) {
          savedDirectory[0] = null;
        } else {
          controller.readDirectory(savedDirectory[0]);
        }
      }
      br.close();
    } catch (IOException ignored) {
    }

    JFrame mf = new JFrame("check docx");
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem readDirectorItem = new JMenuItem("Read directory");
    readDirectorItem.addActionListener(
        e -> {
          JFileChooser fileChooser = new JFileChooser();
          if (savedDirectory[0] != null) {
            fileChooser.setCurrentDirectory(savedDirectory[0]);
          }
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            savedDirectory[0] = fileChooser.getSelectedFile();
            controller.readDirectory(fileChooser.getSelectedFile());
          }
        });
    fileMenu.add(readDirectorItem);
    menuBar.add(fileMenu);
    mf.setJMenuBar(menuBar);
    mf.setContentPane(contentPane);
    mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mf.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            if (savedDirectory[0] != null) {
              try {
                PrintWriter writer = new PrintWriter(new FileWriter(".config"));
                writer.println(savedDirectory[0].getAbsolutePath());
                writer.flush();
                writer.close();
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            }

            super.windowClosing(e);
          }
        });
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    mf.setSize(screenSize.width - 200, screenSize.height - 200);
    mf.setLocation(100, 100);
    mf.setVisible(true);
  }
}
