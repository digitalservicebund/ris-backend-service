package de.bund.digitalservice.ris.checkdocx;

import de.bund.digitalservice.ris.domain.DocxConverterService;
import de.bund.digitalservice.ris.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.domain.docx.ErrorElement;
import de.bund.digitalservice.ris.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.utils.DocxConverter;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CheckDocxController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckDocxController.class);

  private final List<IView> views = new ArrayList<>();

  private Map<String, String> filePaths = new HashMap<>();
  private List<DocumentUnitDocx> contentList = new ArrayList<>();
  private boolean errorsOnly;
  private File selectedFile;
  private Object selectedElement;
  private Map<String, Style> styles;
  private Component selectedTab;
  private String xpathFilter;

  public void addView(IView view) {
    if (views.contains(view)) {
      return;
    }

    views.add(view);
  }

  public void readDirectory(File directory) {
    if (!directory.isDirectory()) {
      return;
    }

    filePaths =
        Arrays.stream(directory.listFiles((dir, name) -> name.endsWith("docx")))
            .collect(Collectors.toMap(File::getName, File::getAbsolutePath));
    notifyAllViews(NotificationType.READ_DIRECTORY);
  }

  public void readDocx(String fileName) {
    DocxConverter converter = new DocxConverter();
    DocxConverterService service = new DocxConverterService(null, null, converter);
    String filePath = filePaths.get(fileName);
    try {
      selectedFile = new File(filePath);
      contentList = service.parseAsDocumentUnitDocxList(new FileInputStream(selectedFile));
      notifyAllViews(NotificationType.SELECT_FILE);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<String> getFiles() {
    return filePaths.keySet();
  }

  public List<DocumentUnitDocx> getConvertedFileContent() {
    if (errorsOnly) {
      return contentList.stream()
          .filter(
              el -> {
                if (el instanceof ParagraphElement paragraphElement) {
                  return paragraphElement.getRunElements().stream()
                      .anyMatch(run -> run instanceof ErrorElement);
                } else return el instanceof ErrorElement;
              })
          .toList();
    } else {
      return contentList;
    }
  }

  public List<Object> getOriginalFileContent() {
    try {
      WordprocessingMLPackage mlPackage = Docx4J.load(selectedFile);
      styles =
          mlPackage
              .getMainDocumentPart()
              .getStyleDefinitionsPart()
              .getContents()
              .getStyle()
              .stream()
              .collect(Collectors.toMap(Style::getStyleId, Function.identity()));
      return mlPackage.getMainDocumentPart().getContent();
    } catch (Docx4JException e) {
      LOGGER.error("Couldn't get file content.", e);
    }

    return Collections.emptyList();
  }

  private void notifyAllViews(NotificationType type) {
    views.forEach(v -> v.update(type));
  }

  public void setErrorsOnly(boolean errorsOnly) {
    this.errorsOnly = errorsOnly;
    notifyAllViews(NotificationType.TOOGLE_ONLY_ERROR);
  }

  public boolean isErrorsOnly() {
    return errorsOnly;
  }

  public void setSelectedElement(Object selectedElement) {
    this.selectedElement = selectedElement;
    notifyAllViews(NotificationType.SELECT_ELEMENT);
  }

  public Object getSelectedElement() {
    return selectedElement;
  }

  public Style getStyleInformation(String key) {
    return styles.get(key);
  }

  public void changeTab(Component selectedTab) {
    this.selectedTab = selectedTab;
    if (selectedTab instanceof CheckDocxConvertedPane) {
      notifyAllViews(NotificationType.SELECT_CONVERTED_TAB);
    } else if (selectedTab instanceof CheckDocxOriginalPane) {
      notifyAllViews(NotificationType.SELECT_ORIGINAL_TAB);
    } else if (selectedTab instanceof CheckDocxXMLPane) {
      notifyAllViews(NotificationType.SELECT_XML_TAB);
    }
  }

  public Component getSelectedTab() {
    return selectedTab;
  }

  public String getXml() {
    try {
      WordprocessingMLPackage mlPackage = Docx4J.load(selectedFile);
      styles =
          mlPackage
              .getMainDocumentPart()
              .getStyleDefinitionsPart()
              .getContents()
              .getStyle()
              .stream()
              .collect(Collectors.toMap(Style::getStyleId, Function.identity()));
      String xml = mlPackage.getMainDocumentPart().getXML();
      if (xpathFilter != null && !xpathFilter.isBlank()) {
        Document document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = xpathFilter;
        NodeList nodeList =
            (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);
          StringWriter sw = new StringWriter();
          Transformer t = TransformerFactory.newInstance().newTransformer();
          t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          t.setOutputProperty(OutputKeys.INDENT, "yes");
          t.transform(new DOMSource(node), new StreamResult(sw));

          stringBuilder.append(sw);
        }
        return stringBuilder.toString();
      }
      return xml;
    } catch (Docx4JException e) {
      LOGGER.error("Couldn't get file content.", e);
    } catch (IOException
        | ParserConfigurationException
        | SAXException
        | XPathExpressionException
        | TransformerException e) {
      LOGGER.error("Couldn't xpath file content.", e);
    }

    return "no xml data found";
  }

  public void filterXML(String xpathFilter) {
    this.xpathFilter = xpathFilter;
    notifyAllViews(NotificationType.FILTER_XML);
  }
}
