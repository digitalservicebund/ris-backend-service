package de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeRepository extends ReactiveSortingRepository<DocumentTypeDTO, Long> {}
