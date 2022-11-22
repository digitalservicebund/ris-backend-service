package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypeRepository;
import java.util.Optional;
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

  public Flux<DocumentType> getDocumentTypes(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return documentTypeRepository
          .findBySearchStr(searchStr.get().trim())
          .map(
              documentTypeDTO ->
                  new DocumentType(
                      documentTypeDTO.getId(),
                      documentTypeDTO.getJurisShortcut(),
                      documentTypeDTO.getLabel()));
    }
    return documentTypeRepository
        .findAllByOrderByJurisShortcutAscLabelAsc()
        .map(
            documentTypeDTO ->
                new DocumentType(
                    documentTypeDTO.getId(),
                    documentTypeDTO.getJurisShortcut(),
                    documentTypeDTO.getLabel()));
  }

  public Flux<Court> getCourts(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return courtRepository
          .findBySearchStr(searchStr.get().trim())
          .map(
              courtDTO ->
                  new Court(
                      courtDTO.getCourttype(),
                      courtDTO.getCourtlocation(),
                      courtDTO.getCourttype() + " " + courtDTO.getCourtlocation()));
    }
    return courtRepository
        .findAllByOrderByCourttypeAscCourtlocationAsc()
        .map(
            courtDTO ->
                new Court(
                    courtDTO.getCourttype(),
                    courtDTO.getCourtlocation(),
                    courtDTO.getCourttype() + " " + courtDTO.getCourtlocation()));
  }
}
