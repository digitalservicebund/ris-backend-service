package de.bund.digitalservice.ris.caselaw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegexTest {
  @SuppressWarnings("java:S5852")
  private static final Pattern TEXT_CHECK_PATTERN =
      Pattern.compile("<text-check.*?>(.*?)</text-check>");

  @ParameterizedTest
  @CsvSource({
    "<text-check>test</text-check>, test",
    "<text-check>test1</text-check> zwischen <text-check>test2</text-check>, test1 zwischen test2",
    "<text-check id=\"1\">test1</text-check> zwischen <text-check valid=\"true\">test2</text-check>, test1 zwischen test2",
  })
  void testRegex(String value, String result) {
    Matcher matcher = TEXT_CHECK_PATTERN.matcher(value);
    StringBuilder builder = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(builder, matcher.group(1));
    }

    assertThat(builder.toString()).hasToString(result);
  }
}
