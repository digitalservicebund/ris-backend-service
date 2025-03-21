package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import static de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService.getAnnotationsArray;

import java.util.stream.Stream;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
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
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("<div>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("Hello, world!", ((JSONObject) result.get(1)).get("text"));
    Assertions.assertEquals("</div>", ((JSONObject) result.get(2)).get("markup"));
  }

  @Test
  void testParagraphAndBreak() {
    String html = "<body><p>Line 1<br>Line 2</p></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(5, result.size());
    Assertions.assertEquals("<p>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("\n\n", ((JSONObject) result.get(0)).get("interpretAs"));
    Assertions.assertEquals("Line 1", ((JSONObject) result.get(1)).get("text"));
    Assertions.assertEquals("<br>", ((JSONObject) result.get(2)).get("markup"));
    Assertions.assertEquals("\n", ((JSONObject) result.get(2)).get("interpretAs"));
    Assertions.assertEquals("Line 2", ((JSONObject) result.get(3)).get("text"));
    Assertions.assertEquals("</p>", ((JSONObject) result.get(4)).get("markup"));
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
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(20, result.size());
    Assertions.assertEquals("<table>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("<colgroup>", ((JSONObject) result.get(1)).get("markup"));
    // verify self-closing tags col are not closed
    Assertions.assertEquals("<col>", ((JSONObject) result.get(2)).get("markup"));
    Assertions.assertEquals("<col>", ((JSONObject) result.get(3)).get("markup"));
    Assertions.assertEquals("</colgroup>", ((JSONObject) result.get(4)).get("markup"));
    // verify tbody is added when missing
    Assertions.assertEquals("<tbody>", ((JSONObject) result.get(5)).get("markup"));
    Assertions.assertEquals("<tr>", ((JSONObject) result.get(6)).get("markup"));
    Assertions.assertEquals("<td>", ((JSONObject) result.get(7)).get("markup"));
    Assertions.assertEquals("<p>", ((JSONObject) result.get(8)).get("markup"));
    Assertions.assertEquals("test", ((JSONObject) result.get(9)).get("text"));
    Assertions.assertEquals("</p>", ((JSONObject) result.get(10)).get("markup"));
    Assertions.assertEquals("</td>", ((JSONObject) result.get(11)).get("markup"));
    Assertions.assertEquals("<td>", ((JSONObject) result.get(12)).get("markup"));
    Assertions.assertEquals("<p>", ((JSONObject) result.get(13)).get("markup"));
    Assertions.assertEquals("table", ((JSONObject) result.get(14)).get("text"));
    Assertions.assertEquals("</p>", ((JSONObject) result.get(15)).get("markup"));
    Assertions.assertEquals("</td>", ((JSONObject) result.get(16)).get("markup"));
    Assertions.assertEquals("</tr>", ((JSONObject) result.get(17)).get("markup"));
    Assertions.assertEquals("</tbody>", ((JSONObject) result.get(18)).get("markup"));
    Assertions.assertEquals("</table>", ((JSONObject) result.get(19)).get("markup"));
  }

  @Test
  void testAttributes() {
    String html = "<body><span class=\"test\" id=\"mySpan\">Test</span></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals(
        "<span class=\"test\" id=\"mySpan\">", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("Test", ((JSONObject) result.get(1)).get("text"));
    Assertions.assertEquals("</span>", ((JSONObject) result.get(2)).get("markup"));
  }

  @Test
  void testEmptyText() {
    String html = "<body><p></p></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("<p>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("</p>", ((JSONObject) result.get(1)).get("markup"));
  }

  @Test
  void testNonBreakingSpace() {
    String html = "<body><p>Test&nbsp;Space</p></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("Test\u00A0Space", ((JSONObject) result.get(1)).get("text"));
  }

  @Test
  void testMultipleElements() {
    String html = "<body><p>Hello</p><span>World</span></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(6, result.size());
    Assertions.assertEquals("<p>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("\n\n", ((JSONObject) result.get(0)).get("interpretAs"));
    Assertions.assertEquals("Hello", ((JSONObject) result.get(1)).get("text"));
    Assertions.assertEquals("</p>", ((JSONObject) result.get(2)).get("markup"));
    Assertions.assertEquals("<span>", ((JSONObject) result.get(3)).get("markup"));
    Assertions.assertEquals("World", ((JSONObject) result.get(4)).get("text"));
    Assertions.assertEquals("</span>", ((JSONObject) result.get(5)).get("markup"));
  }

  @Test
  void testNestedElements() {
    String html = "<body><p><span>Hello</span>World</p></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(6, result.size());
    Assertions.assertEquals("<p>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals("\n\n", ((JSONObject) result.get(0)).get("interpretAs"));
    Assertions.assertEquals("<span>", ((JSONObject) result.get(1)).get("markup"));
    Assertions.assertEquals("Hello", ((JSONObject) result.get(2)).get("text"));
    Assertions.assertEquals("</span>", ((JSONObject) result.get(3)).get("markup"));
    Assertions.assertEquals("World", ((JSONObject) result.get(4)).get("text"));
    Assertions.assertEquals("</p>", ((JSONObject) result.get(5)).get("markup"));
  }

  @Test
  void testSelfClosingTags() {
    // contains img, hr, br
    String html = "<body><div><img src=\"http://example.com/image\"><hr><br></div></body>";
    Document doc = Jsoup.parse(html);
    JSONArray result = getAnnotationsArray(doc);

    Assertions.assertEquals(5, result.size());
    Assertions.assertEquals("<div>", ((JSONObject) result.get(0)).get("markup"));
    Assertions.assertEquals(
        "<img src=\"http://example.com/image\">", ((JSONObject) result.get(1)).get("markup"));
    Assertions.assertEquals("<hr>", ((JSONObject) result.get(2)).get("markup"));
    Assertions.assertEquals("<br>", ((JSONObject) result.get(3)).get("markup"));
    Assertions.assertEquals("</div>", ((JSONObject) result.get(4)).get("markup"));
  }
}
