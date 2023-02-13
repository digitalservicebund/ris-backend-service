package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.KeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Norm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import java.util.List;
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
  @MockBean private SubjectFieldRepository subjectFieldRepository;
  @MockBean private NormRepository normRepository;
  @MockBean private KeywordRepository keywordRepository;

  @Test
  void testGetDocumentTypes() {
    DocumentTypeDTO documentTypeDTO = DocumentTypeDTO.EMPTY;
    documentTypeDTO.setJurisShortcut("ABC");
    documentTypeDTO.setLabel("LabelABC");
    when(documentTypeRepository.findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc('R'))
        .thenReturn(Flux.just(documentTypeDTO));

    StepVerifier.create(service.getCaselawDocumentTypes(Optional.empty()))
        .consumeNextWith(
            documentType -> {
              assertThat(documentType).isInstanceOf(DocumentType.class);
              assertThat(documentType.jurisShortcut()).isEqualTo("ABC");
              assertThat(documentType.label()).isEqualTo("LabelABC");
            })
        .verifyComplete();

    verify(documentTypeRepository).findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc('R');
  }

  @Test
  void testGetTwoDifferentCourts() {
    // court where the location will be intentionally dropped
    CourtDTO courtA = new CourtDTO();
    courtA.setCourttype("ABC");
    courtA.setCourtlocation("Berlin");
    courtA.setSuperiorcourt("Ja");
    courtA.setForeigncountry("Nein");

    // court where the location will be kept
    CourtDTO courtB = new CourtDTO();
    courtB.setCourttype("XYZ");
    courtB.setCourtlocation("Hamburg");
    courtB.setSuperiorcourt("Nein");
    courtB.setForeigncountry("Nein");

    when(courtRepository.findAllByOrderByCourttypeAscCourtlocationAsc())
        .thenReturn(Flux.fromIterable(List.of(courtA, courtB)));

    StepVerifier.create(service.getCourts(Optional.empty()))
        .expectNext(new Court("ABC", null, "ABC", null))
        .expectNext(new Court("XYZ", "Hamburg", "XYZ Hamburg", null))
        .verifyComplete();

    verify(courtRepository).findAllByOrderByCourttypeAscCourtlocationAsc();
  }

  @Test
  void testGetSubjectFields_withoutSearchString() {
    SubjectField expectedSubjectField =
        new SubjectField(
            2L,
            "TS-01-01",
            "stext 2",
            "navbez 2",
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")));

    when(subjectFieldRepository.findAllByParentIdOrderBySubjectFieldNumberAsc(null))
        .thenReturn(Flux.just(expectedSubjectField));

    StepVerifier.create(service.getSubjectFields(Optional.empty()))
        .consumeNextWith(subjectField -> assertThat(subjectField).isEqualTo(expectedSubjectField))
        .verifyComplete();

    verify(subjectFieldRepository).findAllByParentIdOrderBySubjectFieldNumberAsc(null);
  }

  @Test
  void testGetSubjectFields_withSearchString() {
    String searchString = "stext";
    SubjectField expectedSubjectField =
        new SubjectField(
            2L,
            "TS-01-01",
            "stext 2",
            "navbez 2",
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")));

    when(subjectFieldRepository.findBySearchStr(searchString))
        .thenReturn(Flux.just(expectedSubjectField));

    StepVerifier.create(service.getSubjectFields(Optional.of(searchString)))
        .consumeNextWith(subjectField -> assertThat(subjectField).isEqualTo(expectedSubjectField))
        .verifyComplete();

    verify(subjectFieldRepository).findBySearchStr(searchString);
  }

  @Test
  void testGetSubjectFieldChildren() {
    SubjectField expectedSubjectField =
        new SubjectField(
            2L,
            "TS-01-01",
            "stext 2",
            "navbez 2",
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")));

    when(subjectFieldRepository.findAllByParentIdOrderBySubjectFieldNumberAsc(1L))
        .thenReturn(Flux.just(expectedSubjectField));

    StepVerifier.create(service.getSubjectFieldChildren(1L))
        .consumeNextWith(subjectField -> assertThat(subjectField).isEqualTo(expectedSubjectField))
        .verifyComplete();

    verify(subjectFieldRepository).findAllByParentIdOrderBySubjectFieldNumberAsc(1L);
  }
}
