package de.bund.digitalservice.ris.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import de.bund.digitalservice.ris.repository.DocUnitRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class DocUnitServiceTest {
  @Autowired private DocUnitService service;

  @MockBean private DocUnitRepository repository;

  @Test
  public void testGenerateNewDocUnit() {
    var toSave = new DocUnit();
    toSave.setS3path("filename");
    toSave.setFiletype("docx");

    var savedDocUnit = new DocUnit();
    savedDocUnit.setId(1);
    savedDocUnit.setS3path("filename");
    savedDocUnit.setFiletype("docx");
    when(repository.save(any(DocUnit.class))).thenReturn(Mono.just(savedDocUnit));

    var filePart = mock(FilePart.class);
    doReturn("filename").when(filePart).filename();

    StepVerifier.create(service.generateNewDocUnit(filePart))
        .consumeNextWith(
            docUnit -> {
              assertNotNull(docUnit);
              assertEquals(ResponseEntity.ok(savedDocUnit), docUnit);
            })
        .verifyComplete();
  }

  @Test
  public void testGetAll() {
    StepVerifier.create(service.getAll())
        .consumeNextWith(Assertions::assertNotNull)
        .verifyComplete();

    verify(repository).findAll();
  }
}
