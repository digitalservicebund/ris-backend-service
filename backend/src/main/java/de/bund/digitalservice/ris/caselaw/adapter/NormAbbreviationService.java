package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviationRepository;
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

  public Mono<NormAbbreviation> getNormAbbreviationById(UUID uuid) {
    return repository.findById(uuid);
  }

  public Flux<NormAbbreviation> getNormAbbreviationBySearchQuery(
      String query, Integer size, Integer page) {

    Integer pageOffset = null;
    if (page != null && size != null) {
      pageOffset = page * size;
    }

    return repository.findBySearchQuery(query, size, pageOffset);
  }

  public Flux<NormAbbreviation> getNormAbbreviationByAwesomeSearchQuery(
      String query, Integer size, Integer page) {

    Integer pageOffset = null;
    if (page != null && size != null) {
      pageOffset = page * size;
    }

    return repository.findByAwesomeSearchQuery(query, size, pageOffset);
  }
}
