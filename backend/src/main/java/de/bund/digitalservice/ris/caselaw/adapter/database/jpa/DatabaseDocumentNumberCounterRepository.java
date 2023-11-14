package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentNumberCounterRepository
    extends JpaRepository<DocumentNumberCounterDTO, Integer> {}
