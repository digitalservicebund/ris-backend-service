package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.KeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Norm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class LookupTableService {

  private final DocumentTypeRepository documentTypeRepository;
  private final CourtRepository courtRepository;
  private final SubjectFieldRepository subjectFieldRepository;
  private final KeywordRepository keywordRepository;
  private final NormRepository normRepository;

  public LookupTableService(
      DocumentTypeRepository documentTypeRepository,
      CourtRepository courtRepository,
      SubjectFieldRepository subjectFieldRepository,
      KeywordRepository keywordRepository,
      NormRepository normRepository) {
    this.documentTypeRepository = documentTypeRepository;
    this.courtRepository = courtRepository;
    this.subjectFieldRepository = subjectFieldRepository;
    this.keywordRepository = keywordRepository;
    this.normRepository = normRepository;
  }

  public Flux<DocumentType> getCaselawDocumentTypes(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return documentTypeRepository
          .findCaselawBySearchStr(searchStr.get().trim())
          .map(
              documentTypeDTO ->
                  new DocumentType(documentTypeDTO.getJurisShortcut(), documentTypeDTO.getLabel()));
    }
    return documentTypeRepository
        .findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc('R')
        .map(
            documentTypeDTO ->
                new DocumentType(documentTypeDTO.getJurisShortcut(), documentTypeDTO.getLabel()));
  }

  public Flux<Court> getCourts(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return courtRepository.findBySearchStr(searchStr.get().trim()).map(this::buildCort);
    }
    return courtRepository.findAllByOrderByCourttypeAscCourtlocationAsc().map(this::buildCort);
  }

  // YYYY-MM-DD
  private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

  private String extractRevoked(String additional) {
    if (additional == null || additional.isBlank()) {
      return null;
    }
    additional = additional.toLowerCase();
    if (additional.contains("aufgehoben")) {
      String revoked = "aufgehoben";
      Matcher matcher = DATE_PATTERN.matcher(additional);
      if (matcher.find()) {
        revoked += " seit: " + matcher.group().substring(0, 4);
      }
      return revoked;
    }
    // detect more patterns?
    return null;
  }

  private Court buildCort(CourtDTO courtDTO) {
    String revoked = extractRevoked(courtDTO.getAdditional());
    if (courtDTO.getSuperiorcourt().equalsIgnoreCase("ja")
        && courtDTO.getForeigncountry().equalsIgnoreCase("nein")) {
      return new Court(courtDTO.getCourttype(), null, courtDTO.getCourttype(), revoked);
    }
    return new Court(
        courtDTO.getCourttype(),
        courtDTO.getCourtlocation(),
        courtDTO.getCourttype() + " " + courtDTO.getCourtlocation(),
        revoked);
  }

  public Flux<SubjectField> getSubjectFields(Optional<String> searchStr) {

    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return subjectFieldRepository
          .findBySearchStr(searchStr.get().trim())
          .map(this::buildSubjectField);
    }
    return subjectFieldRepository
        .findAllByParentIdOrderBySubjectFieldNumberAsc(null)
        .map(this::buildSubjectField); // TODO will this case actually get triggered?
  }

  public Flux<SubjectField> getSubjectFieldChildren(Long id) {
    return subjectFieldRepository
        .findAllByParentIdOrderBySubjectFieldNumberAsc(id)
        .map(this::buildSubjectField);
  }

  private SubjectField buildSubjectField(SubjectFieldDTO subjectFieldDTO) {
    return SubjectField.builder()
        .id(subjectFieldDTO.getId())
        .subjectFieldNumber(subjectFieldDTO.getSubjectFieldNumber())
        .subjectFieldText(subjectFieldDTO.getSubjectFieldText())
        .navigationTerm(subjectFieldDTO.getNavigationTerm())
        .build();
  }

  public Flux<Keyword> getSubjectFieldKeywords(long subjectFieldId) {
    return keywordRepository
        .findAllBySubjectFieldIdOrderByValueAsc(subjectFieldId)
        .map(keywordDTO -> Keyword.builder().value(keywordDTO.getValue()).build());
  }

  public Flux<Norm> getSubjectFieldNorms(long subjectFieldId) {
    return normRepository
        .findAllBySubjectFieldIdOrderByAbbreviationAscSingleNormDescriptionAsc(subjectFieldId)
        .map(
            normDTO ->
                Norm.builder()
                    .abbreviation(normDTO.getAbbreviation())
                    .singleNormDescription(normDTO.getSingleNormDescription())
                    .build());
  }
}
