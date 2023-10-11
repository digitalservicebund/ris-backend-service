package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NormAbbreviationService {
  private final NormAbbreviationRepository repository;

  public NormAbbreviationService(NormAbbreviationRepository repository) {
    this.repository = repository;
  }

  public NormAbbreviation getNormAbbreviationById(UUID uuid) {
    return repository.findById(uuid);
  }

  public Flux<NormAbbreviation> getNormAbbreviationsStartingWithExact(
      String query, Integer size, Integer page) {
    return repository.getNormAbbreviationsStartingWithExact(query, size, page);
  }

  public Mono<List<NormAbbreviation>> findAllNormAbbreviationsContaining(
      String query, Integer size, Integer page) {
    return repository.findAllContainingOrderByAccuracy(query, size, page);
  }

  public Mono<Void> refreshMaterializedViews() {
    return repository.refreshMaterializedViews();
  }
}
