package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
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

  public LookupTableService(
      DocumentTypeRepository documentTypeRepository, CourtRepository courtRepository) {
    this.documentTypeRepository = documentTypeRepository;
    this.courtRepository = courtRepository;
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
}
