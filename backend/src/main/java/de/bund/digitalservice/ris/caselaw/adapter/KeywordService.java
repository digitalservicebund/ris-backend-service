package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.KeywordRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class KeywordService {
  private final KeywordRepository repository;

  public KeywordService(KeywordRepository repository) {
    this.repository = repository;
  }

  public Mono<List<String>> getKeywordsForDocumentUnit(UUID documentUnitUuid) {
    return repository.findAllByDocumentUnit(documentUnitUuid);
  }

  public Mono<List<String>> addKeywordToDocumentUnit(UUID documentUnitUuid, String keyword) {
    return repository.addKeywordToDocumentUnit(documentUnitUuid, keyword);
  }

  public Mono<List<String>> deleteKeywordFromDocumentUnit(UUID documentUnitUuid, String keyword) {
    return repository.deleteKeywordFromDocumentUnit(documentUnitUuid, keyword);
  }
}
