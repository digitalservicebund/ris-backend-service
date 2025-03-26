package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import static de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService.getAnnotationsArray;

import com.google.gson.JsonArray;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LanguageToolServiceTest {

  @Test
  void testSimpleText() {
    String html = "<body><div>Hello, world!</div></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("<div>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "Hello, world!", result.get(1).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</div>", result.get(2).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testParagraphAndBreak() {
    String html = "<body><p>Line 1<br>Line 2</p></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(5, result.size());
    Assertions.assertEquals("<p>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "\n\n", result.get(0).getAsJsonObject().get("interpretAs").getAsString());
    Assertions.assertEquals("Line 1", result.get(1).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("<br>", result.get(2).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("\n", result.get(2).getAsJsonObject().get("interpretAs").getAsString());
    Assertions.assertEquals("Line 2", result.get(3).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</p>", result.get(4).getAsJsonObject().get("markup").getAsString());
  }

  public static Stream<String> getTableHtml() {
    String withTBody =
        """
          <table>
            <colgroup>
              <col>
              <col>
            </colgroup>
            <tbody>
              <tr>
                <td>
                  <p>test</p>
                </td>
                <td>
                  <p>table</p>
                </td>
              </tr>
            </tbody>
          </table>""";

    String withoutTBody =
        """
              <table>
                <colgroup>
                  <col>
                  <col>
                </colgroup>
                  <tr>
                    <td>
                      <p>test</p>
                    </td>
                    <td>
                      <p>table</p>
                    </td>
                  </tr>
              </table>""";

    return Stream.of(withTBody.replaceAll("\\s+", ""), withoutTBody.replaceAll("\\s+", ""));
  }

  @ParameterizedTest
  @MethodSource("getTableHtml")
  void testTableWithSelfClosingTags(String tableHtml) {
    Document doc = Jsoup.parse(tableHtml);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(20, result.size());
    Assertions.assertEquals("<table>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "<colgroup>", result.get(1).getAsJsonObject().get("markup").getAsString());
    // verify self-closing tags col are not closed
    Assertions.assertEquals("<col>", result.get(2).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<col>", result.get(3).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "</colgroup>", result.get(4).getAsJsonObject().get("markup").getAsString());
    // verify tbody is added when missing
    Assertions.assertEquals("<tbody>", result.get(5).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<tr>", result.get(6).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<td>", result.get(7).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<p>", result.get(8).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("test", result.get(9).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</p>", result.get(10).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("</td>", result.get(11).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<td>", result.get(12).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<p>", result.get(13).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("table", result.get(14).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</p>", result.get(15).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("</td>", result.get(16).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("</tr>", result.get(17).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "</tbody>", result.get(18).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "</table>", result.get(19).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testAttributes() {
    String html = "<body><span class=\"test\" id=\"mySpan\">Test</span></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals(
        "<span class=\"test\" id=\"mySpan\">",
        result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("Test", result.get(1).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</span>", result.get(2).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testEmptyText() {
    String html = "<body><p></p></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("<p>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("</p>", result.get(1).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testNonBreakingSpace() {
    String html = "<body><p>Test&nbsp;Space</p></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals(
        "Test\u00A0Space", result.get(1).getAsJsonObject().get("text").getAsString());
  }

  @Test
  void testMultipleElements() {
    String html = "<body><p>Hello</p><span>World</span></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(6, result.size());
    Assertions.assertEquals("<p>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "\n\n", result.get(0).getAsJsonObject().get("interpretAs").getAsString());
    Assertions.assertEquals("Hello", result.get(1).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</p>", result.get(2).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<span>", result.get(3).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("World", result.get(4).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</span>", result.get(5).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testNestedElements() {
    String html = "<body><p><span>Hello</span>World</p></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(6, result.size());
    Assertions.assertEquals("<p>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "\n\n", result.get(0).getAsJsonObject().get("interpretAs").getAsString());
    Assertions.assertEquals("<span>", result.get(1).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("Hello", result.get(2).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</span>", result.get(3).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("World", result.get(4).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</p>", result.get(5).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testSelfClosingTags() {
    // contains img, hr, br
    String html = "<body><div><img src=\"http://example.com/image\"><hr><br></div></body>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(5, result.size());
    Assertions.assertEquals("<div>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "<img src=\"http://example.com/image\">",
        result.get(1).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<hr>", result.get(2).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<br>", result.get(3).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("</div>", result.get(4).getAsJsonObject().get("markup").getAsString());
  }

  @Test
  void testKeepEncodedGtAndLtChars() {
    String html = "<p>This text contains a fake &lt;tag&gt; and text afterwards</p>";
    Document doc = Jsoup.parse(html);
    JsonArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(7, result.size());
    Assertions.assertEquals("<p>", result.get(0).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(
        "This text contains a fake ", result.get(1).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("&lt;", result.get(2).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals("<", result.get(2).getAsJsonObject().get("interpretAs").getAsString());
    Assertions.assertEquals("tag", result.get(3).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("&gt;", result.get(4).getAsJsonObject().get("markup").getAsString());
    Assertions.assertEquals(">", result.get(4).getAsJsonObject().get("interpretAs").getAsString());
    Assertions.assertEquals(
        " and text afterwards", result.get(5).getAsJsonObject().get("text").getAsString());
    Assertions.assertEquals("</p>", result.get(6).getAsJsonObject().get("markup").getAsString());
  }
}
