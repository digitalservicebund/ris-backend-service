package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import de.bund.digitalservice.ris.caselaw.domain.docx.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class StyleDocxConverterTest {

  @ParameterizedTest(name = "#{index} - Run test with args={0}")
  @NullSource
  @ValueSource(strings = {"", "wrong style string; another wrong style"})
  void testGetListFromString(String stylesStr) {

    List<Style> styleList = StyleConverter.getListFromString(stylesStr);

    assertThat(styleList).isEmpty();
  }

  @Test
  void testGetListFromString_withOneStyleWithEndingSemicolon() {

    List<Style> styleList = StyleConverter.getListFromString("style-name:style-value;");

    assertThat(styleList)
        .extracting("property", "value")
        .containsExactly(
            tuple("style-name", new ArrayList<>(Collections.singleton("style-value"))));
  }

  @Test
  void testGetListFromString_withOneStyleWithoutEndingSemicolon() {

    List<Style> styleList = StyleConverter.getListFromString("style-name:style-value");

    assertThat(styleList)
        .extracting("property", "value")
        .containsExactly(
            tuple("style-name", new ArrayList<>(Collections.singleton("style-value"))));
  }

  @Test
  void testGetListFromString_withThreeStylesWithoutSpaces() {

    List<Style> styleList =
        StyleConverter.getListFromString(
            "style-name:style-value;style-name2:style-value2;style-name3:style-value3;");

    assertThat(styleList)
        .extracting("property", "value")
        .containsExactly(
            tuple("style-name", new ArrayList<>(Collections.singleton("style-value"))),
            tuple("style-name2", new ArrayList<>(Collections.singleton("style-value2"))),
            tuple("style-name3", new ArrayList<>(Collections.singleton("style-value3"))));
  }

  @Test
  void testGetListFromString_withThreeStylesWithSpaces() {

    List<Style> styleList =
        StyleConverter.getListFromString(
            "style-name :   style-value; style-name2 :style-value2    ;style-name3   :style-value3  ");

    assertThat(styleList)
        .extracting("property", "value")
        .containsExactly(
            tuple("style-name", new ArrayList<>(Collections.singleton("style-value"))),
            tuple("style-name2", new ArrayList<>(Collections.singleton("style-value2"))),
            tuple("style-name3", new ArrayList<>(Collections.singleton("style-value3"))));
  }

  @Test
  void testGetListFromString_withThreeStylesWithOneWrong() {

    List<Style> styleList =
        StyleConverter.getListFromString(
            "style-name ; style-name2 :style-value2    ;style-name3   :style-value3  ");

    assertThat(styleList)
        .extracting("property", "value")
        .containsExactly(
            tuple("style-name2", new ArrayList<>(Collections.singleton("style-value2"))),
            tuple("style-name3", new ArrayList<>(Collections.singleton("style-value3"))));
  }
}
