package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.KeywordRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({KeywordService.class})
class KeywordServiceTest {
  @Autowired KeywordService service;

  @MockBean KeywordRepository repository;

  @Test
  void testGetAllKeywordsForDocumentUnit_shouldReturnList() {
    UUID documentUnitUuid = UUID.randomUUID();
    when(repository.findAllByDocumentUnit(documentUnitUuid)).thenReturn(Mono.empty());

    StepVerifier.create(service.getKeywordsForDocumentUnit(documentUnitUuid)).verifyComplete();

    verify(repository, times(1)).findAllByDocumentUnit(documentUnitUuid);
  }

  @Test
  void testAddKeywordToDocumentUnit_shouldReturnList() {
    UUID documentUnitUuid = UUID.randomUUID();
    when(repository.addKeywordToDocumentUnit(documentUnitUuid, "test")).thenReturn(Mono.empty());

    StepVerifier.create(service.addKeywordToDocumentUnit(documentUnitUuid, "test"))
        .verifyComplete();

    verify(repository, times(1)).addKeywordToDocumentUnit(documentUnitUuid, "test");
  }

  @Test
  void testDeleteKeywordFromDocumentUnit_shouldReturnList() {
    UUID documentUnitUuid = UUID.randomUUID();
    when(repository.deleteKeywordFromDocumentUnit(documentUnitUuid, "test"))
        .thenReturn(Mono.empty());

    StepVerifier.create(service.deleteKeywordFromDocumentUnit(documentUnitUuid, "test"))
        .verifyComplete();

    verify(repository, times(1)).deleteKeywordFromDocumentUnit(documentUnitUuid, "test");
  }
}
