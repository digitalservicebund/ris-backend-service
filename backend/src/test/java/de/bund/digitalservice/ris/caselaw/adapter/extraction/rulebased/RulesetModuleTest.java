package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RulesetModuleTest {
  private final RulesetModule extractor = RulesetModule.fromFile("ner.json");

  @ParameterizedTest
  @MethodSource("provideNerTests")
  void testNer(String text, Map<String, String> expected) {
    HtmlElement doc = JsoupParser.parse("<p>" + text + "</p>");
    HtmlElement tag = doc.findAll("p", true).get(0);

    ExtractionContext ctx = new ExtractionContext(List.of(tag));
    TagData tagData = new TagData(tag, 0, tag.innerText(), null);
    extractor.processTag(tagData, ctx);

    Map<String, String> result = new HashMap<>();
    for (Extraction e : ctx.getExtractions()) {
      result.put(e.extractionClass(), e.extractionText());
    }

    assertThat(result).isEqualTo(expected);
  }

  private static Stream<Arguments> provideNerTests() {
    return Stream.of(
        Arguments.of(
            "Vorinstanz:AGH Berlin, Urteil vom 06.11.2024 - II AGH 3/23 - ",
            Map.of(
                "court", "AGH Berlin",
                "document_type", "Urteil",
                "date", "06.11.2024",
                "file_number", "II AGH 3/23")),
        Arguments.of("2 KLs 500 Js 22402/22", Map.of("file_number", "2 KLs 500 Js 22402/22")),
        Arguments.of("VIII KLs 104 Js 27144/23", Map.of("file_number", "VIII KLs 104 Js 27144/23")),
        Arguments.of(
            "15 KLs 500 Js 23178/20 jug", Map.of("file_number", "15 KLs 500 Js 23178/20 jug")),
        Arguments.of(
            "2 KLs 500 Js 22402/22 (3)", Map.of("file_number", "2 KLs 500 Js 22402/22 (3)")),
        Arguments.of(
            "26 KLs 22 Js 21579/21 (5/23)", Map.of("file_number", "26 KLs 22 Js 21579/21 (5/23)")),
        Arguments.of("12 KLs 119/24 jug (3)", Map.of("file_number", "12 KLs 119/24 jug (3)")),
        Arguments.of("Sächsisches FG", Map.of("court", "Sächsisches FG")),
        Arguments.of("LG Berlin II", Map.of("court", "LG Berlin II")),
        Arguments.of("5 K 2911/18 U", Map.of("file_number", "5 K 2911/18 U")),
        Arguments.of(
            "FG des Landes Sachsen-Anhalt", Map.of("court", "FG des Landes Sachsen-Anhalt")),
        Arguments.of("FG des Saarlandes", Map.of("court", "FG des Saarlandes")),
        Arguments.of("9 K 349/22 G", Map.of("file_number", "9 K 349/22 G")),
        Arguments.of("6 K 1427/20 Z", Map.of("file_number", "6 K 1427/20 Z")),
        Arguments.of("6 K 3388/16 K", Map.of("file_number", "6 K 3388/16 K")),
        Arguments.of("4 K 1280/21 AO", Map.of("file_number", "4 K 1280/21 AO")),
        Arguments.of("8 K 998/21 GrE", Map.of("file_number", "8 K 998/21 GrE")),
        Arguments.of("4 K 896/20 Erb", Map.of("file_number", "4 K 896/20 Erb")),
        Arguments.of("16a U 172/19", Map.of("file_number", "16a U 172/19")),
        Arguments.of("2-23 O 177/20", Map.of("file_number", "2-23 O 177/20")),
        Arguments.of(
            "Entscheidung vom 28.09.2018 - 7 O 165/16 - ",
            Map.of(
                "document_type", "Entscheidung",
                "date", "28.09.2018",
                "file_number", "7 O 165/16")),
        Arguments.of("25 XIV 123/21 (B)", Map.of("file_number", "25 XIV 123/21 (B)")),
        Arguments.of("Schleswig-Holsteinisches FG", Map.of("court", "Schleswig-Holsteinisches FG")),
        Arguments.of("I-34 U 141/19", Map.of("file_number", "I-34 U 141/19")),
        Arguments.of("V ZR 35/21     Verkündet", Map.of("file_number", "V ZR 35/21")),
        Arguments.of(
            "Oberlandesgericht Karlsruhe Beschluss",
            Map.of("court", "Oberlandesgericht Karlsruhe", "document_type", "Beschluss")),
        Arguments.of(
            "FINANZGERICHT BADEN-WÜRTTEMBERG", Map.of("court", "FINANZGERICHT BADEN-WÜRTTEMBERG")),
        Arguments.of("3 Ws 353/23\n32", Map.of("file_number", "3 Ws 353/23")),
        Arguments.of("\n", Map.of()),
        Arguments.of(
            "1 F 136/24 AG Weinheim", Map.of("file_number", "1 F 136/24", "court", "AG Weinheim")),
        Arguments.of(
            "10a KLs 506 Js 41452/21 (4/22)",
            Map.of("file_number", "10a KLs 506 Js 41452/21 (4/22)")),
        Arguments.of("27 XVII S 3644", Map.of("file_number", "27 XVII S 3644")),
        Arguments.of(
            "68 KLs-609 Js 1029/21-18/21", Map.of("file_number", "68 KLs-609 Js 1029/21-18/21")));
  }
}
