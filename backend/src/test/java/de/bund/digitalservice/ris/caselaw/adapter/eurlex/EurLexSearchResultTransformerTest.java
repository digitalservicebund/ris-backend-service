package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
class EurLexSearchResultTransformerTest {
  @Test
  void testTransformDTOToDomain() {
    Instant now = Instant.now();
    EurLexResultDTO dto =
        EurLexResultDTO.builder()
            .resultXml("<xml><result>s/result>")
            .celex("celex")
            .court(CourtDTO.builder().type("court-type").location("court-location").build())
            .uri("uri")
            .date(LocalDate.of(2024, Month.DECEMBER, 24))
            .ecli("ecli")
            .htmlLink("html-link")
            .status(EurLexResultStatus.NEW)
            .title("title with file number T-123/45")
            .createdAt(now)
            .publicationDate(LocalDate.of(2025, Month.JANUARY, 2))
            .build();
    SearchResult expected =
        SearchResult.builder()
            .courtType("court-type")
            .courtLocation("court-location")
            .uri("uri")
            .celex("celex")
            .htmlLink("html-link")
            .ecli("ecli")
            .title("title with file number T-123/45")
            .fileNumber("T-123/45")
            .date(LocalDate.of(2024, Month.DECEMBER, 24))
            .publicationDate(LocalDate.ofInstant(now, ZoneId.systemDefault()))
            .build();

    SearchResult result = EurLexSearchResultTransformer.transformDTOToDomain(dto);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDTOToDomain_withoutFileNumberInTitle_hasNoFileNumberInDomainObject() {
    EurLexResultDTO dto = EurLexResultDTO.builder().title("title without file number").build();
    SearchResult expected = SearchResult.builder().fileNumber(null).build();

    SearchResult result = EurLexSearchResultTransformer.transformDTOToDomain(dto);

    assertThat(result).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "title with file number T-123/45, T-123/45",
    "title with file number C-123/45, C-123/45",
    "title with file number T-123/45\u00A0abc, T-123/45\u00A0abc",
    "title with file number C-123/45\u00A0abc, C-123/45\u00A0abc",
    "title with file number T-123/45\u00A0abc and T-987/65\u00A0zyx, T-123/45\u00A0abc, T-987/65\u00A0zyx",
    "title with file number C-123/45\u00A0abc and C-987/65\u00A0zyx, C-123/45\u00A0abc, C-987/65\u00A0zyx"
  })
  void testTransformDTOToDomain_withFileNumberWithAllowedPattern_hasParsedFileNumber(
      String title, String fileNumbers) {
    EurLexResultDTO dto = EurLexResultDTO.builder().title(title).build();
    SearchResult expected = SearchResult.builder().title(title).fileNumber(fileNumbers).build();

    SearchResult result = EurLexSearchResultTransformer.transformDTOToDomain(dto);

    assertThat(result).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "title with file number T-12345\u00A0abc",
        "title with file number T-12/345\u00A0123",
        "title with file number T-12/345 abc",
        "title with file number T-12a45\u00A0abc"
      })
  void testTransformDTOToDomain_withFileNumberWithWrongFormat_hasNoFileNumberInDomainObject(
      String title) {
    EurLexResultDTO dto = EurLexResultDTO.builder().title(title).build();
    SearchResult expected = SearchResult.builder().title(title).fileNumber(null).build();

    SearchResult result = EurLexSearchResultTransformer.transformDTOToDomain(dto);

    assertThat(result).isEqualTo(expected);
  }
}
