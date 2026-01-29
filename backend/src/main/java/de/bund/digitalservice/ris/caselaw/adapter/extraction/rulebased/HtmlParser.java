package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

class JsoupElement implements HtmlElement {
  private final Element element;
  private final JsoupParser parser;
  private final String elementName;
  private final Pos position;

  JsoupElement(Element element, JsoupParser parser) {
    this.element = element;
    this.parser = parser;
    this.elementName = element.tagName();
    this.position = parser.getOffsets(element);
  }

  @Override
  public String name() {
    return elementName;
  }

  @Override
  public Pos pos() {
    return position;
  }

  @Override
  public String innerText() {
    return element.text(); // TODO: newline separator "\n" between elements
  }

  @Override
  public boolean isCentered() {
    String style = element.attr("style");
    return style != null && style.toLowerCase().contains("text-align: center");
  }

  @Override
  // public String outerHtml() { return element.outerHtml(); }
  public String outerHtml() {
    if (position != null) {
      try {
        return parser.html.substring(position.start(), position.end());
      } catch (Exception e) {
        return element.outerHtml(); // Fallback
      }
    }
    return element.outerHtml();
  }

  @Override
  public HtmlElement find(String selector, boolean recursive) {
    List<Element> elements = select(selector == null ? "*" : selector, recursive);
    return elements.isEmpty() ? null : new JsoupElement(elements.get(0), parser);
  }

  @Override
  public List<HtmlElement> findAll(String selector, boolean recursive) {
    return select(selector == null ? "*" : selector, recursive).stream()
        .map(e -> (HtmlElement) new JsoupElement(e, parser))
        .toList();
  }

  private List<Element> select(String selector, boolean recursive) {
    return recursive ? element.select(selector) : element.select(":root > " + selector);
  }
}

class JsoupParser {
  public final String html;
  private final Document document;
  private static Parser parser = Parser.htmlParser().setTrackPosition(true);

  public JsoupParser(String html) {
    this.html = html;
    this.document = parser.parseInput(html, "");
  }

  public static HtmlElement parse(String html) {
    JsoupParser parser = new JsoupParser(html);
    return new JsoupElement(parser.document, parser);
  }

  Pos getOffsets(Element element) {
    var openingRange = element.sourceRange();
    var closingRange = element.endSourceRange();
    return openingRange.isTracked()
        ? new Pos(openingRange.start().pos(), closingRange.end().pos())
        : null;
  }
}
