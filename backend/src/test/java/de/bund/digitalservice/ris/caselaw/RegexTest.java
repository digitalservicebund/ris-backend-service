package de.bund.digitalservice.ris.caselaw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class RegexTest {
  private static final Pattern TEXT_CHECK_PATTERN =
      Pattern.compile("<text-check.*?>(.*?)</text-check>");

  @Test
  void testRegex() {
    String value = "<text-check>test</text-check>";

    Matcher matcher = TEXT_CHECK_PATTERN.matcher(value);
    StringBuilder builder = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(builder, matcher.group(1));
    }

    assertThat(builder.toString()).hasToString("test");
  }

  @Test
  void testRegex2() {
    String value = "<text-check>test1</text-check> zwischen <text-check>test2</text-check>";

    Matcher matcher = TEXT_CHECK_PATTERN.matcher(value);
    StringBuilder builder = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(builder, matcher.group(1));
    }

    assertThat(builder.toString()).hasToString("test1 zwischen test2");
  }

  @Test
  void testRegex3() {
    String value =
        "<text-check id=\"1\">test1</text-check> zwischen <text-check valid=\"true\">test2</text-check>";

    Matcher matcher = TEXT_CHECK_PATTERN.matcher(value);
    StringBuilder builder = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(builder, matcher.group(1));
    }

    assertThat(builder.toString()).hasToString("test1 zwischen test2");
  }
}
