package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.KeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldRepository;
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
  void testGetSubjectFields() {
    SubjectFieldDTO subjectFieldDTO =
        new SubjectFieldDTO(
            2L,
            1L,
            false,
            "2022-12-22",
            "2022-12-24",
            'J',
            "1.0",
            "TS-01-01",
            "stext 2",
            "navbez 2",
            false);

    when(subjectFieldRepository.findAllByParentIdOrderBySubjectFieldNumberAsc(null))
        .thenReturn(Flux.just(subjectFieldDTO));

    StepVerifier.create(service.getSubjectFields(Optional.empty()))
        .consumeNextWith(
            subjectField -> {
              assertThat(subjectField).isInstanceOf(SubjectField.class);
              assertThat(subjectField.id()).isEqualTo(subjectFieldDTO.getId());
              assertThat(subjectField.subjectFieldNumber())
                  .isEqualTo(subjectFieldDTO.getSubjectFieldNumber());
              assertThat(subjectField.subjectFieldText())
                  .isEqualTo(subjectFieldDTO.getSubjectFieldText());
              assertThat(subjectField.navigationTerm())
                  .isEqualTo(subjectFieldDTO.getNavigationTerm());
            })
        .verifyComplete();

    verify(subjectFieldRepository).findAllByParentIdOrderBySubjectFieldNumberAsc(null);
  }

  @Test
  void testGetSubjectFieldChildren() {
    SubjectFieldDTO subjectFieldDTO =
        new SubjectFieldDTO(
            2L,
            1L,
            false,
            "2022-12-22",
            "2022-12-24",
            'J',
            "1.0",
            "TS-01-01",
            "stext 2",
            "navbez 2",
            false);

    when(subjectFieldRepository.findAllByParentIdOrderBySubjectFieldNumberAsc(1L))
        .thenReturn(Flux.just(subjectFieldDTO));

    StepVerifier.create(service.getSubjectFieldChildren(1L))
        .consumeNextWith(
            subjectField -> {
              assertThat(subjectField).isInstanceOf(SubjectField.class);
              assertThat(subjectField.id()).isEqualTo(subjectFieldDTO.getId());
              assertThat(subjectField.subjectFieldNumber())
                  .isEqualTo(subjectFieldDTO.getSubjectFieldNumber());
              assertThat(subjectField.subjectFieldText())
                  .isEqualTo(subjectFieldDTO.getSubjectFieldText());
              assertThat(subjectField.navigationTerm())
                  .isEqualTo(subjectFieldDTO.getNavigationTerm());
            })
        .verifyComplete();

    verify(subjectFieldRepository).findAllByParentIdOrderBySubjectFieldNumberAsc(1L);
  }

  @Test
  void testGetSubjectFieldKeywords() {
    KeywordDTO keywordDTO = KeywordDTO.builder().subjectFieldId(2L).value("schlagwort 2.1").build();

    when(keywordRepository.findAllBySubjectFieldIdOrderByValueAsc(2L))
        .thenReturn(Flux.just(keywordDTO));

    StepVerifier.create(service.getSubjectFieldKeywords(2L))
        .consumeNextWith(
            keyword -> {
              assertThat(keyword).isInstanceOf(Keyword.class);
              assertThat(keyword.value()).isEqualTo(keywordDTO.getValue());
            })
        .verifyComplete();

    verify(keywordRepository).findAllBySubjectFieldIdOrderByValueAsc(2L);
  }

  @Test
  void testGetSubjectFieldNorms() {
    NormDTO normDTO =
        NormDTO.builder().subjectFieldId(2L).shortcut("normabk 2.1").enbez("§ 2.1").build();

    when(normRepository.findAllBySubjectFieldIdOrderByShortcutAscEnbezAsc(2L))
        .thenReturn(Flux.just(normDTO));

    StepVerifier.create(service.getSubjectFieldNorms(2L))
        .consumeNextWith(
            norm -> {
              assertThat(norm).isInstanceOf(Norm.class);
              assertThat(norm.shortcut()).isEqualTo(normDTO.getShortcut());
              assertThat(norm.enbez()).isEqualTo(normDTO.getEnbez());
            })
        .verifyComplete();

    verify(normRepository).findAllBySubjectFieldIdOrderByShortcutAscEnbezAsc(2L);
  }
}
