package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import(LookupTableService.class)
class LookupTableServiceTest {

  @SpyBean private LookupTableService service;

  @MockBean private DocumentTypeRepository documentTypeRepository;
  @MockBean private CourtRepository courtRepository;

  @Test
  void testGetDocumentTypes() {
    DocumentTypeDTO documentTypeDTO = DocumentTypeDTO.EMPTY;
    documentTypeDTO.setId(3L);
    when(documentTypeRepository.findAll()).thenReturn(Flux.just(documentTypeDTO));

    StepVerifier.create(service.getDocumentTypes(Optional.empty()))
        .consumeNextWith(
            documentType -> {
              assertThat(documentType).isInstanceOf(DocumentType.class);
              assertThat(documentType.id()).isEqualTo(3L);
            })
        .verifyComplete();

    verify(documentTypeRepository).findAll();
  }

  @Test
  void testGetCourts() {
    CourtDTO courtDTO = CourtDTO.EMPTY;
    courtDTO.setCourttype("BGH");
    when(courtRepository.findAll()).thenReturn(Flux.just(courtDTO));

    StepVerifier.create(service.getCourts(Optional.empty()))
        .consumeNextWith(
            court -> {
              assertThat(court).isInstanceOf(Court.class);
              assertThat(court.type()).isEqualTo("BGH");
            })
        .verifyComplete();

    verify(courtRepository).findAll();
  }
}
