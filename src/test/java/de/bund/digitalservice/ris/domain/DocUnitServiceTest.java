package de.bund.digitalservice.ris.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(SpringExtension.class)
@Import(DocUnitService.class)
@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
class DocUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String RECEIVER_ADDRESS = "test@exporter.neuris";
  @SpyBean private DocUnitService service;

  @MockBean private DocUnitRepository repository;

  @MockBean private DocumentUnitListEntryRepository listEntryRepository;

  @MockBean private DocumentNumberCounterRepository counterRepository;

  @MockBean private PreviousDecisionRepository previousDecisionRepository;

  @MockBean private S3AsyncClient s3AsyncClient;

  @MockBean private EmailPublishService publishService;

  @Test
  void testGenerateNewDocUnit() {
    when(repository.save(any(DocUnitDTO.class))).thenReturn(Mono.just(DocUnitDTO.EMPTY));
    when(counterRepository.getDocumentNumberCounterEntry())
        .thenReturn(Mono.just(DocumentNumberCounter.buildInitial()));
    when(counterRepository.save(any(DocumentNumberCounter.class)))
        .thenReturn(Mono.just(DocumentNumberCounter.buildInitial()));
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    StepVerifier.create(service.generateNewDocUnit(DocUnitCreationInfo.EMPTY))
        .expectNextCount(1) // That it's a DocUnit is given by the generic type..
        .verifyComplete();
    verify(repository).save(any(DocUnitDTO.class));
  }

  // @Test public void testGenerateNewDocUnit_withException() {}

  @Test
  void testAttachFileToDocUnit() {
    // given
    var byteBufferFlux = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of("content/type"));
    headerMap.put("X-Filename", List.of("testfile.docx"));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);

    var toSave = new DocUnitDTO();
    toSave.setUuid(TEST_UUID);
    toSave.setS3path(TEST_UUID.toString());
    toSave.setFiletype("docx");
    toSave.setFilename("testfile.docx");

    var savedDocUnit = new DocUnitDTO();
    savedDocUnit.setUuid(TEST_UUID);
    savedDocUnit.setS3path(TEST_UUID.toString());
    savedDocUnit.setFiletype("docx");
    when(repository.save(any(DocUnitDTO.class))).thenReturn(Mono.just(savedDocUnit));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(savedDocUnit));

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    var asyncRequestBodyCaptor = ArgumentCaptor.forClass(AsyncRequestBody.class);

    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(TEST_UUID);

      // when and then
      StepVerifier.create(service.attachFileToDocUnit(TEST_UUID, byteBufferFlux, httpHeaders))
          .consumeNextWith(
              docUnit -> {
                assertNotNull(docUnit);
                assertEquals(savedDocUnit, docUnit);
              })
          .verifyComplete();

      verify(s3AsyncClient)
          .putObject(putObjectRequestCaptor.capture(), asyncRequestBodyCaptor.capture());
      assertEquals("testBucket", putObjectRequestCaptor.getValue().bucket());
      assertEquals(TEST_UUID.toString(), putObjectRequestCaptor.getValue().key());
      assertEquals("content/type", putObjectRequestCaptor.getValue().contentType());
      StepVerifier.create(asyncRequestBodyCaptor.getValue())
          .expectNext(ByteBuffer.wrap(new byte[] {}))
          .verifyComplete();
      toSave.setFileuploadtimestamp(savedDocUnit.getFileuploadtimestamp());
      verify(repository).save(toSave);
    }
  }

  @Test
  void testRemoveFileFromDocUnit() {
    var docUnitBefore = new DocUnitDTO();
    docUnitBefore.setUuid(TEST_UUID);
    docUnitBefore.setS3path(TEST_UUID.toString());
    docUnitBefore.setFilename("testfile.docx");

    var docUnitAfter = new DocUnitDTO();
    docUnitAfter.setUuid(TEST_UUID);

    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(docUnitBefore));
    // is the thenReturn ok? Or am I bypassing the actual functionality-test?
    when(repository.save(any(DocUnitDTO.class))).thenReturn(Mono.just(docUnitAfter));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.removeFileFromDocUnit(TEST_UUID))
        .consumeNextWith(
            docUnitResponseEntity -> {
              assertNotNull(docUnitResponseEntity);
              assertEquals(HttpStatus.OK, docUnitResponseEntity.getStatusCode());
              assertEquals(docUnitAfter, docUnitResponseEntity.getBody());
            })
        .verifyComplete();

    ArgumentCaptor<DocUnitDTO> docUnitCaptor = ArgumentCaptor.forClass(DocUnitDTO.class);
    verify(repository).save(docUnitCaptor.capture());
    assertEquals(docUnitCaptor.getValue(), docUnitAfter);
  }

  @Test
  void testGenerateNewDocUnitAndAttachFile_withExceptionFromBucket() throws S3Exception {
    // given
    var byteBufferFlux = ByteBuffer.wrap(new byte[] {});

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenThrow(SdkException.create("exception", null));

    // when and then
    StepVerifier.create(service.attachFileToDocUnit(TEST_UUID, byteBufferFlux, HttpHeaders.EMPTY))
        .expectErrorMatches(ex -> ex instanceof SdkException)
        .verify();

    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    verify(repository, times(0)).save(any(DocUnitDTO.class));
  }

  @Test
  void testGenerateNewDocUnitAndAttachFile_withExceptionFromRepository() {
    // given
    var byteBufferFlux = ByteBuffer.wrap(new byte[] {});

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));
    doThrow(new IllegalArgumentException()).when(repository).save(any(DocUnitDTO.class));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocUnitDTO.EMPTY));

    // when and then
    StepVerifier.create(service.attachFileToDocUnit(TEST_UUID, byteBufferFlux, HttpHeaders.EMPTY))
        .expectErrorMatches(ex -> ex instanceof IllegalArgumentException)
        .verify();

    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    verify(repository).save(any(DocUnitDTO.class));
  }

  @Test
  void testGetAll() {
    StepVerifier.create(service.getAll())
        .consumeNextWith(Assertions::assertNotNull)
        .verifyComplete();

    verify(listEntryRepository).findAll(Sort.by(Order.desc("documentnumber")));
  }

  @Test
  void testGetByDocumentnumber() {
    when(repository.findByDocumentnumber("ABCDE2022000001"))
        .thenReturn(Mono.just(DocUnitDTO.EMPTY));
    when(previousDecisionRepository.findAllByDocumentnumber("ABCDE2022000001"))
        .thenReturn(Flux.just(new PreviousDecision()));
    StepVerifier.create(service.getByDocumentnumber("ABCDE2022000001"))
        .consumeNextWith(
            monoResponse -> assertEquals(monoResponse.getBody().getClass(), DocUnitDTO.class))
        .verifyComplete();
    verify(repository).findByDocumentnumber("ABCDE2022000001");
  }

  @Nested
  @DisplayName("Test Update DocUnit With PreviousDecisions")
  class TestUpdateDocUnitWithPreviousDecisions {
    private List<PreviousDecision> previousDecisionsList;
    private List<String> previousDecisionsIdsToDelete;
    private Long count;
    private final String documentNr = "ABCDE2022000001";

    private List<Long> getRemainsIds() {
      return previousDecisionsList.stream().map(previousDecision -> previousDecision.id).toList();
    }

    private Mono<Void> deleteByIds(List<String> ids) {
      previousDecisionsList.removeAll(
          previousDecisionsList.stream()
              .filter(previousDecision -> ids.contains(String.valueOf(previousDecision.id)))
              .toList());
      return null;
    }

    private List<PreviousDecision> saveAll(List<PreviousDecision> pDecisionsList) {
      List<PreviousDecision> pDecisionToInsert =
          pDecisionsList.stream()
              .filter(previousDecision -> previousDecision.id == null)
              .map(
                  previousDecision -> {
                    previousDecision.id = Long.valueOf(++count);
                    return previousDecision;
                  })
              .toList();
      pDecisionToInsert.forEach(previousDecision -> previousDecisionsList.add(previousDecision));
      List<PreviousDecision> pDecisionToUpdate =
          pDecisionsList.stream().filter(previousDecision -> previousDecision.id != null).toList();
      pDecisionToUpdate.forEach(
          decisionToUpdate -> {
            previousDecisionsList =
                new ArrayList<>(
                    previousDecisionsList.stream()
                        .map(
                            pd -> {
                              if (pd.id == decisionToUpdate.id) {
                                return decisionToUpdate;
                              }
                              return pd;
                            })
                        .toList());
          });
      return pDecisionsList;
    }

    @BeforeEach
    void setUp() {
      previousDecisionsIdsToDelete = new ArrayList<>();
      previousDecisionsList = new ArrayList<>();
      previousDecisionsList.add(
          new PreviousDecision(
              1L, "gerTyp 1", "gerOrt 1", "01.01.2022", "aktenzeichen 1", "ABCDE2022000001"));
      previousDecisionsList.add(
          new PreviousDecision(
              2L, "gerTyp 2", "gerOrt 2", "01.02.2022", "aktenzeichen 2", "ABCDE2022000001"));
      previousDecisionsList.add(
          new PreviousDecision(
              3L, "gerTyp 3", "gerOrt 3", "01.03.2022", "aktenzeichen 3", "ABCDE2022000001"));
      previousDecisionsList.add(
          new PreviousDecision(
              4L, "gerTyp 4", "gerOrt 4", "01.04.2022", "aktenzeichen 4", "ABCDE2022000001"));
      previousDecisionsList.add(
          new PreviousDecision(
              5L, "gerTyp 5", "gerOrt 5", "01.05.2022", "aktenzeichen 5", "ABCDE2022000001"));
      count = Long.valueOf(previousDecisionsList.size());
    }

    @Test
    void testGetByDocumentnumberWithPreviousDecisions() {
      when(repository.findByDocumentnumber(documentNr)).thenReturn(Mono.just(DocUnitDTO.EMPTY));
      when(previousDecisionRepository.findAllByDocumentnumber(documentNr))
          .thenReturn(Flux.fromIterable(previousDecisionsList));
      StepVerifier.create(service.getByDocumentnumber(documentNr))
          .consumeNextWith(
              monoResponse -> {
                assertEquals(monoResponse.getBody().previousDecisions.size(), count);
                PreviousDecision previousDecision = monoResponse.getBody().previousDecisions.get(0);
                assertEquals(previousDecision.id, 1L);
                assertEquals(previousDecision.courtPlace, "gerOrt 1");
                assertEquals(previousDecision.courtType, "gerTyp 1");
                assertEquals(previousDecision.date, "01.01.2022");
                assertEquals(previousDecision.docketNumber, "aktenzeichen 1");
                assertEquals(previousDecision.documentnumber, documentNr);
              })
          .verifyComplete();
      verify(repository).findByDocumentnumber("ABCDE2022000001");
    }

    @Test
    void testUpdateDocUnitWithPreviousDecisionsDelete() {
      previousDecisionsIdsToDelete.add("2");
      previousDecisionsIdsToDelete.add("4");
      var remainPreviousDecision =
          previousDecisionsList.stream()
              .filter(
                  previousDecision -> !previousDecisionsIdsToDelete.contains(previousDecision.id))
              .toList();
      var docUnit = DocUnitDTO.EMPTY.setPreviousDecisions(remainPreviousDecision);
      when(previousDecisionRepository.getAllIdsByDocumentnumber(documentNr))
          .thenReturn(
              Flux.fromIterable(
                  previousDecisionsList.stream()
                      .map(previousDecision -> previousDecision.id)
                      .toList()));
      when(previousDecisionRepository.deleteAllById(previousDecisionsIdsToDelete))
          .thenReturn(deleteByIds(previousDecisionsIdsToDelete));
      when(previousDecisionRepository.saveAll(remainPreviousDecision))
          .thenReturn(Flux.fromIterable(saveAll(remainPreviousDecision)));
      when(repository.save(docUnit)).thenReturn(Mono.just(docUnit));

      StepVerifier.create(service.updateDocUnit(docUnit))
          .consumeNextWith(
              monoResponse -> {
                assertEquals(previousDecisionsList.size(), 3);
                List<Long> remainIds = getRemainsIds();
                assertTrue(remainIds.contains(1L));
                assertTrue(remainIds.contains(3L));
                assertTrue(remainIds.contains(5L));
                assertFalse(remainIds.contains(2L));
                assertFalse(remainIds.contains(4L));
                assertEquals(monoResponse.getBody(), docUnit);
              })
          .verifyComplete();
      verify(repository).save(docUnit);
    }

    @Test
    void testUpdateDocUnitWithPreviousDecisionsInsert() {
      var remainPreviousDecision = new ArrayList<>(previousDecisionsList);
      remainPreviousDecision.add(
          new PreviousDecision(
              null, "gerTyp 6", "gerOrt 6", "01.01.2022", "aktenzeichen 6", documentNr));
      remainPreviousDecision.add(
          new PreviousDecision(
              null, "gerTyp 7", "gerOrt 7", "01.01.2022", "aktenzeichen 7", documentNr));
      var docUnit = DocUnitDTO.EMPTY.setPreviousDecisions(remainPreviousDecision);
      when(previousDecisionRepository.getAllIdsByDocumentnumber(documentNr))
          .thenReturn(
              Flux.fromIterable(
                  previousDecisionsList.stream()
                      .map(previousDecision -> previousDecision.id)
                      .toList()));
      when(previousDecisionRepository.deleteAllById(previousDecisionsIdsToDelete))
          .thenReturn(deleteByIds(previousDecisionsIdsToDelete));
      when(previousDecisionRepository.saveAll(remainPreviousDecision))
          .thenReturn(Flux.fromIterable(saveAll(remainPreviousDecision)));
      when(repository.save(docUnit)).thenReturn(Mono.just(docUnit));

      StepVerifier.create(service.updateDocUnit(docUnit))
          .consumeNextWith(
              monoResponse -> {
                assertEquals(previousDecisionsList.size(), 7);
                List<Long> remainIds = getRemainsIds();
                assertTrue(remainIds.contains(6L));
                assertTrue(remainIds.contains(7L));
                PreviousDecision previousDecision =
                    monoResponse.getBody().previousDecisions.get(previousDecisionsList.size() - 2);
                assertEquals(previousDecision.id, 6L);
                assertEquals(previousDecision.courtPlace, "gerOrt 6");
                assertEquals(previousDecision.courtType, "gerTyp 6");
                assertEquals(previousDecision.date, "01.01.2022");
                assertEquals(previousDecision.docketNumber, "aktenzeichen 6");
                previousDecision =
                    monoResponse.getBody().previousDecisions.get(previousDecisionsList.size() - 1);
                assertEquals(previousDecision.id, 7L);
                assertEquals(previousDecision.courtPlace, "gerOrt 7");
                assertEquals(previousDecision.courtType, "gerTyp 7");
                assertEquals(previousDecision.date, "01.01.2022");
                assertEquals(previousDecision.docketNumber, "aktenzeichen 7");
                assertEquals(monoResponse.getBody(), docUnit);
              })
          .verifyComplete();
      verify(repository).save(docUnit);
    }

    @Test
    void testUpdateDocUnitWithPreviousDecisionsUpdate() {
      var remainPreviousDecision = new ArrayList<>(previousDecisionsList);
      remainPreviousDecision.get(0).courtPlace = "new gerOrt";
      remainPreviousDecision.get(0).courtType = "new gerTyp";
      remainPreviousDecision.get(0).date = "30.01.2022";
      remainPreviousDecision.get(0).docketNumber = "new aktenzeichen";
      var docUnit = DocUnitDTO.EMPTY.setPreviousDecisions(remainPreviousDecision);
      when(previousDecisionRepository.getAllIdsByDocumentnumber(documentNr))
          .thenReturn(
              Flux.fromIterable(
                  previousDecisionsList.stream()
                      .map(previousDecision -> previousDecision.id)
                      .toList()));
      when(previousDecisionRepository.deleteAllById(previousDecisionsIdsToDelete))
          .thenReturn(deleteByIds(previousDecisionsIdsToDelete));
      when(previousDecisionRepository.saveAll(remainPreviousDecision))
          .thenReturn(Flux.fromIterable(saveAll(remainPreviousDecision)));
      when(repository.save(docUnit)).thenReturn(Mono.just(docUnit));

      StepVerifier.create(service.updateDocUnit(docUnit))
          .consumeNextWith(
              monoResponse -> {
                assertEquals(
                    monoResponse.getBody().previousDecisions.size(), previousDecisionsList.size());
                assertTrue(
                    monoResponse.getBody().previousDecisions.containsAll(previousDecisionsList));
                assertEquals(monoResponse.getBody(), docUnit);
              })
          .verifyComplete();
      verify(repository).save(docUnit);
    }

    @Test
    void testUpdateDocUnitWithPreviousDecisionsInsertUpdateDelete() {
      previousDecisionsIdsToDelete.add("2");
      previousDecisionsIdsToDelete.add("4");
      List<PreviousDecision> remainPreviousDecision =
          new ArrayList<>(
              previousDecisionsList.stream()
                  .filter(
                      previousDecision ->
                          !previousDecisionsIdsToDelete.contains(previousDecision.id))
                  .toList());
      remainPreviousDecision.get(0).courtPlace = "new gerOrt";
      remainPreviousDecision.get(0).courtType = "new gerTyp";
      remainPreviousDecision.get(0).date = "30.01.2022";
      remainPreviousDecision.get(0).docketNumber = "new aktenzeichen";
      remainPreviousDecision.add(
          new PreviousDecision(
              null, "gerTyp 6", "gerOrt 6", "01.01.2022", "aktenzeichen 6", documentNr));
      remainPreviousDecision.add(
          new PreviousDecision(
              null, "gerTyp 7", "gerOrt 7", "01.01.2022", "aktenzeichen 7", documentNr));
      var docUnit = DocUnitDTO.EMPTY.setPreviousDecisions(remainPreviousDecision);
      when(previousDecisionRepository.getAllIdsByDocumentnumber(documentNr))
          .thenReturn(
              Flux.fromIterable(
                  previousDecisionsList.stream()
                      .map(previousDecision -> previousDecision.id)
                      .toList()));
      when(previousDecisionRepository.deleteAllById(previousDecisionsIdsToDelete))
          .thenReturn(deleteByIds(previousDecisionsIdsToDelete));
      when(previousDecisionRepository.saveAll(remainPreviousDecision))
          .thenReturn(Flux.fromIterable(saveAll(remainPreviousDecision)));
      when(repository.save(docUnit)).thenReturn(Mono.just(docUnit));

      StepVerifier.create(service.updateDocUnit(docUnit))
          .consumeNextWith(
              monoResponse -> {
                assertEquals(previousDecisionsList.size(), 5);
                PreviousDecision previousDecision = monoResponse.getBody().previousDecisions.get(0);
                assertEquals(previousDecision.id, 1L);
                List<Long> remainIds = getRemainsIds();
                assertTrue(remainIds.contains(6L));
                assertTrue(remainIds.contains(7L));
                assertFalse(remainIds.contains(2L));
                assertFalse(remainIds.contains(4L));
                assertEquals(previousDecision.courtPlace, "new gerOrt");
                assertEquals(previousDecision.courtType, "new gerTyp");
                assertEquals(previousDecision.date, "30.01.2022");
                assertEquals(previousDecision.docketNumber, "new aktenzeichen");
                assertEquals(monoResponse.getBody(), docUnit);
              })
          .verifyComplete();
      verify(repository).save(docUnit);
    }
  }

  @Test
  void testDeleteByUuid_withoutFileAttached() {
    // I think I shouldn't have to insert a specific DocUnit object here?
    // But if I don't, the test by itself succeeds, but fails if all tests in this class run
    // something flaky with the repository mock? Investigate this later
    DocUnitDTO docUnit = new DocUnitDTO();
    docUnit.setUuid(TEST_UUID);
    // can we also test that the fileUuid from the DocUnit is used? with a captor somehow?
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(docUnit));
    when(repository.delete(any(DocUnitDTO.class))).thenReturn(Mono.just(mock(Void.class)));

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
              assertEquals("done", stringResponseEntity.getBody());
            })
        .verifyComplete();

    verify(s3AsyncClient, times(0)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withFileAttached() {
    DocUnitDTO docUnit = new DocUnitDTO();
    docUnit.setUuid(TEST_UUID);
    docUnit.setS3path(TEST_UUID.toString());
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(docUnit));
    when(repository.delete(any(DocUnitDTO.class))).thenReturn(Mono.just(mock(Void.class)));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
              assertEquals("done", stringResponseEntity.getBody());
            })
        .verifyComplete();

    verify(s3AsyncClient, times(1)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromBucket() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocUnitDTO.EMPTY));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenThrow(SdkException.create("exception", null));

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
              assertEquals("Couldn't delete the DocUnit", stringResponseEntity.getBody());
            })
        .verifyComplete();
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocUnitDTO.EMPTY));
    doThrow(new IllegalArgumentException()).when(repository).delete(DocUnitDTO.EMPTY);

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
              assertEquals("Couldn't delete the DocUnit", stringResponseEntity.getBody());
            })
        .verifyComplete();
  }

  @Test
  void testUpdateDocUnit() {
    var docUnit = DocUnitDTO.EMPTY;
    when(repository.save(docUnit)).thenReturn(Mono.just(docUnit));
    StepVerifier.create(service.updateDocUnit(docUnit))
        .consumeNextWith(monoResponse -> assertEquals(monoResponse.getBody(), docUnit))
        .verifyComplete();
    verify(repository).save(docUnit);
  }

  @Test
  void testPublishByEmail() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocUnitDTO.EMPTY));
    XmlMail xmlMail =
        new XmlMail(
            1L,
            123L,
            "receiver address",
            "subject",
            "xml",
            "200",
            "status messages",
            "filename",
            null);
    when(publishService.publish(DocUnitDTO.EMPTY, RECEIVER_ADDRESS))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));
    when(previousDecisionRepository.findAllByDocumentnumber(DocUnitDTO.EMPTY.documentnumber))
        .thenReturn(Flux.just(new PreviousDecision()));
    StepVerifier.create(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS))
        .consumeNextWith(
            mailResponse ->
                assertThat(mailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).publish(DocUnitDTO.EMPTY, RECEIVER_ADDRESS);
  }

  @Test
  void testPublishByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.empty());

    StepVerifier.create(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS)).verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService, never()).publish(DocUnitDTO.EMPTY, RECEIVER_ADDRESS);
  }

  @Test
  void testGetLastPublishedXmlMail() {
    DocUnitDTO documentUnit = new DocUnitDTO();
    documentUnit.setId(123L);
    XmlMail xmlMail =
        new XmlMail(
            1L, 123L, "receiver address", "subject", "xml", "200", "message", "filename", null);
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnit));
    when(publishService.getLastPublishedXml(123L, TEST_UUID))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));

    StepVerifier.create(service.getLastPublishedXmlMail(TEST_UUID))
        .consumeNextWith(
            xmlMailResponse ->
                assertThat(xmlMailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).getLastPublishedXml(123L, TEST_UUID);
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
