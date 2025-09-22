package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import static de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService.getAnnotationsArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  LanguageToolService.class,
})
class LanguageToolServiceTest {

  @MockitoSpyBean LanguageToolService languageToolService;

  @MockitoBean LanguageToolClient languageToolClient;
  @MockitoBean LanguageToolConfig languageToolConfig;
  @MockitoBean DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean IgnoredTextCheckWordRepository ignoredTextCheckWordRepository;
  @MockitoBean FeatureToggleService featureToggleService;

  @Test
  void testRequestTool_withCategoriesWithAllowedRules() {
    when(languageToolConfig.getDisabledCategoriesWithWhitelistedRules())
        .thenReturn(
            Map.of(
                "DISABLED_CATEGORY_1", List.of("ENABLED_RULE_1", "ENABLED_RULE_2"),
                "DISABLED_CATEGORY_2", List.of("ENABLED_RULE_3")));

    when(languageToolConfig.isEnabled()).thenReturn(true);

    when(languageToolClient.checkText(any()))
        .thenReturn(
            LanguageToolResponse.builder()
                .matches(
                    List.of(
                        // not filtered because in RANDOM_CATEGORY_1 is not restricted
                        createMatch("RANDOM_RULE_1", "RANDOM_CATEGORY_1"),
                        // filtered because only rules ENABLED_RULE_1 and ENABLED_RULE_2 are allowed
                        // in DISABLED_CATEGORY_1
                        createMatch("RANDOM_RULE_2", "DISABLED_CATEGORY_1"),
                        // not filtered because rule ENABLED_RULE_1 is allowed in
                        // DISABLED_CATEGORY_1
                        createMatch("ENABLED_RULE_1", "DISABLED_CATEGORY_1"),
                        // filtered because only rule ENABLED_RULE_3 is allowed in
                        // DISABLED_CATEGORY_2
                        createMatch("ENABLED_RULE_2", "DISABLED_CATEGORY_2")))
                .build());

    String html = "<body><div>Hello, world!</div></body>";

    var response = languageToolService.requestTool(html);
    Assertions.assertEquals(2, response.size());
    Assertions.assertEquals("RANDOM_RULE_1", response.getFirst().rule().id());
    Assertions.assertEquals("RANDOM_CATEGORY_1", response.getFirst().rule().category().id());
    Assertions.assertEquals("ENABLED_RULE_1", response.get(1).rule().id());
    Assertions.assertEquals("DISABLED_CATEGORY_1", response.get(1).rule().category().id());
  }

  private static Match createMatch(String rule, String category) {
    return Match.builder()
        .rule(Rule.builder().id(rule).category(Category.builder().id(category).build()).build())
        .build();
  }

  @Test
  void shouldReturnEmptyListWhenDisabled() {
    when(languageToolConfig.isEnabled()).thenReturn(false);
    Assertions.assertEquals(0, languageToolService.requestTool("").size());
  }

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
