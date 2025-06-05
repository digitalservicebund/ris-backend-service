package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseEurLexResultRepository extends JpaRepository<EurLexResultDTO, UUID> {
  Optional<EurLexResultDTO> findTopByOrderByCreatedAtDesc();

  List<EurLexResultDTO> findAllByCelexIn(List<String> celexNumbers);

  List<EurLexResultDTO> deleteAllByCelexIn(List<String> celexNumbers);

  Optional<EurLexResultDTO> findByCelex(String celexNumber);
}
