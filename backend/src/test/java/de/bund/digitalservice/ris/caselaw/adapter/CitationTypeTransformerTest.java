package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CitationTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CitationTypeTransformerTest {

  @ParameterizedTest
  @CsvSource({
    "4e768071-1a19-43a1-8ab9-c185adec94bf, Anwendung', Anwendung",
    "6b4bd747-fce9-4e49-8af4-3fb4f1d3663c, Nan, Nichtanwendung"
  })
  void shouldTransformToDomain(String id, String abbreviation, String label) {
    CitationTypeDTO citationTypeDTO =
        CitationTypeDTO.builder()
            .id(UUID.fromString(id))
            .abbreviation(abbreviation)
            .label(label)
            .build();

    CitationType citationType = CitationTypeTransformer.transformToDomain(citationTypeDTO);

    // Assertion
    assertThat(citationType)
        .extracting("jurisShortcut", "label")
        .containsExactly(abbreviation, label);
  }

  @Test
  void shouldNotTransformToDomain() {
    CitationTypeDTO citationTypeDTO = null;

    assertThat(CitationTypeTransformer.transformToDomain(citationTypeDTO)).isNull();
  }
}
