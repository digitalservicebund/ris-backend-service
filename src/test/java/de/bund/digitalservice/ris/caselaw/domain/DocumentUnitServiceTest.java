package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitTransformer;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
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
@Import(DocumentUnitService.class)
@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
class DocumentUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String RECEIVER_ADDRESS = "test@exporter.neuris";
  @SpyBean private DocumentUnitService service;

  @MockBean private DocumentUnitRepository repository;

  @MockBean private DocumentUnitListEntryRepository listEntryRepository;

  @MockBean private DocumentNumberService documentNumberService;

  @MockBean private S3AsyncClient s3AsyncClient;

  @MockBean private EmailPublishService publishService;

  @Test
  void testGenerateNewDocumentUnit() {
    when(repository.createNewDocumentUnit("nextDocumentNumber"))
        .thenReturn(Mono.just(DocumentUnit.EMPTY));
    when(documentNumberService.generateNextDocumentNumber(DocumentUnitCreationInfo.EMPTY))
        .thenReturn(Mono.just("nextDocumentNumber"));
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    StepVerifier.create(service.generateNewDocumentUnit(DocumentUnitCreationInfo.EMPTY))
        .expectNextCount(1) // That it's a DocumentUnit is given by the generic type..
        .verifyComplete();
    verify(documentNumberService).generateNextDocumentNumber(DocumentUnitCreationInfo.EMPTY);
    verify(repository).createNewDocumentUnit("nextDocumentNumber");
  }

  // @Test public void testGenerateNewDocumentUnit_withException() {}

  @Test
  void testAttachFileToDocumentUnit() {
    // given
    var byteBuffer = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of("content/type"));
    headerMap.put("X-Filename", List.of("testfile.docx"));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);

    var toSave =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .s3path(TEST_UUID.toString())
            .filetype("docx")
            .filename("testfile.docx")
            .build();

    var savedDocumentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .s3path(TEST_UUID.toString())
            .filetype("docx")
            .build();
    when(repository.attachFile(TEST_UUID, TEST_UUID.toString(), "docx", "testfile.docx"))
        .thenReturn(Mono.just(savedDocumentUnit));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(savedDocumentUnit));

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    var asyncRequestBodyCaptor = ArgumentCaptor.forClass(AsyncRequestBody.class);

    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(TEST_UUID);

      // when and then
      StepVerifier.create(service.attachFileToDocumentUnit(TEST_UUID, byteBuffer, httpHeaders))
          .consumeNextWith(
              documentUnit -> {
                assertNotNull(documentUnit);
                assertEquals(savedDocumentUnit, documentUnit);
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
      verify(repository).attachFile(TEST_UUID, TEST_UUID.toString(), "docx", "testfile.docx");
    }
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    var documentUnitBefore =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .s3path(TEST_UUID.toString())
            .filename("testfile.docx")
            .build();

    var documentUnitAfter = DocumentUnit.builder().uuid(TEST_UUID).build();

    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnitBefore));
    // is the thenReturn ok? Or am I bypassing the actual functionality-test?
    when(repository.removeFile(TEST_UUID)).thenReturn(Mono.just(documentUnitAfter));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.removeFileFromDocumentUnit(TEST_UUID))
        .consumeNextWith(
            documentUnit -> {
              assertNotNull(documentUnit);
              assertEquals(documentUnitAfter, documentUnit);
            })
        .verifyComplete();

    verify(repository).removeFile(TEST_UUID);
  }

  @Test
  void testGenerateNewDocumentUnitAndAttachFile_withExceptionFromBucket() throws S3Exception {
    // given
    var byteBuffer = ByteBuffer.wrap(new byte[] {});

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenThrow(SdkException.create("exception", null));

    // when and then
    StepVerifier.create(service.attachFileToDocumentUnit(TEST_UUID, byteBuffer, HttpHeaders.EMPTY))
        .expectErrorMatches(ex -> ex instanceof SdkException)
        .verify();

    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    verify(repository, times(0)).save(any(DocumentUnit.class));
  }

  @Test
  void testGetAll() {
    when(listEntryRepository.findAll(Sort.by(Order.desc("documentnumber"))))
        .thenReturn(Flux.empty());

    StepVerifier.create(service.getAll())
        .consumeNextWith(Assertions::assertNotNull)
        .verifyComplete();

    verify(listEntryRepository).findAll(Sort.by(Order.desc("documentnumber")));
  }

  @Test
  void testGetByDocumentnumber() {
    when(repository.findByDocumentNumber("ABCDE20220001"))
        .thenReturn(Mono.just(DocumentUnit.EMPTY));
    StepVerifier.create(service.getByDocumentNumber("ABCDE20220001"))
        .consumeNextWith(documentUnit -> assertEquals(documentUnit.getClass(), DocumentUnit.class))
        .verifyComplete();
    verify(repository).findByDocumentNumber("ABCDE20220001");
  }

  //  @Nested
  //  @DisplayName("Test Update DocumentUnit With PreviousDecisions")
  //  class TestUpdateDocumentUnitWithPreviousDecisions {
  //    private List<PreviousDecision> previousDecisionsListInDB;
  //    private List<String> previousDecisionsIdsToDelete;
  //    private List<PreviousDecision> inputPreviousDecisionFromFE;
  //    private Long count;
  //    private final String documentNr = "ABCDE20220001";
  //
  //    private record DocumentUnitObj(DocumentUnit documentUnit, DocumentUnitDTO documentUnitDTO)
  // {}
  //
  //    private DocumentUnitObj setUpMockDBQueries() {
  //      DocumentUnit documentUnit =
  //          DocumentUnitBuilder.newInstance()
  //              .setDocumentUnitDTO(DocumentUnitDTO.EMPTY)
  //              .setId(99L)
  //              .setUUID(UUID.randomUUID())
  //              .setDocumentNumber(documentNr)
  //              .setPreviousDecisions(inputPreviousDecisionFromFE)
  //              .setCreationtimestamp(Instant.now())
  //              .setFileuploadtimestamp(Instant.now())
  //              .build();
  //      when(previousDecisionRepository.getAllIdsByDocumentnumber(documentNr))
  //          .thenReturn(
  //              Flux.fromIterable(
  //                  previousDecisionsListInDB.stream()
  //                      .map(previousDecision -> previousDecision.id)
  //                      .toList()));
  //      when(previousDecisionRepository.deleteAllById(deleteByIds(previousDecisionsIdsToDelete)))
  //          .thenReturn(Mono.empty());
  //      when(previousDecisionRepository.saveAll(inputPreviousDecisionFromFE))
  //          .thenReturn(Flux.fromIterable(saveAll()));
  //      var documentUnitDTO = DocumentUnitDTO.buildFromDocumentUnit(documentUnit);
  //      when(repository.save(documentUnitDTO)).thenReturn(Mono.just(documentUnitDTO));
  //      return new DocumentUnitObj(documentUnit, documentUnitDTO);
  //    }
  //
  //    private List<Long> getRemainsIds() {
  //      return previousDecisionsListInDB.stream()
  //          .map(previousDecision -> previousDecision.id)
  //          .toList();
  //    }
  //
  //    private List<String> deleteByIds(List<String> ids) {
  //      previousDecisionsListInDB.removeAll(
  //          previousDecisionsListInDB.stream()
  //              .filter(previousDecision -> ids.contains(String.valueOf(previousDecision.id)))
  //              .toList());
  //      return ids;
  //    }
  //
  //    private List<PreviousDecision> saveAll() {
  //      List<PreviousDecision> pDecisionToInsert =
  //          inputPreviousDecisionFromFE.stream()
  //              .filter(previousDecision -> previousDecision.id == null)
  //              .map(
  //                  previousDecision -> {
  //                    previousDecision.id = Long.valueOf(++count);
  //                    return previousDecision;
  //                  })
  //              .toList();
  //      if (pDecisionToInsert.size() > 0) {
  //        pDecisionToInsert.forEach(
  //            previousDecision -> previousDecisionsListInDB.add(previousDecision));
  //      }
  //      List<PreviousDecision> pDecisionToUpdate =
  //          inputPreviousDecisionFromFE.stream()
  //              .filter(previousDecision -> previousDecision.id != null)
  //              .toList();
  //      if (pDecisionToUpdate.size() > 0) {
  //        pDecisionToUpdate.forEach(
  //            decisionToUpdate -> {
  //              previousDecisionsListInDB =
  //                  new ArrayList<>(
  //                      previousDecisionsListInDB.stream()
  //                          .map(
  //                              pd -> {
  //                                if (pd.id == decisionToUpdate.id) {
  //                                  return decisionToUpdate;
  //                                }
  //                                return pd;
  //                              })
  //                          .toList());
  //            });
  //      }
  //      return previousDecisionsListInDB;
  //    }
  //
  //    @BeforeEach
  //    void setUp() {
  //      previousDecisionsIdsToDelete = new ArrayList<>();
  //      previousDecisionsListInDB = new ArrayList<>();
  //      previousDecisionsListInDB.add(
  //          new PreviousDecision(
  //              1L, "gerTyp 1", "gerOrt 1", "01.01.2022", "aktenzeichen 1", "ABCDE20220001"));
  //      previousDecisionsListInDB.add(
  //          new PreviousDecision(
  //              2L, "gerTyp 2", "gerOrt 2", "01.02.2022", "aktenzeichen 2", "ABCDE20220001"));
  //      previousDecisionsListInDB.add(
  //          new PreviousDecision(
  //              3L, "gerTyp 3", "gerOrt 3", "01.03.2022", "aktenzeichen 3", "ABCDE20220001"));
  //      previousDecisionsListInDB.add(
  //          new PreviousDecision(
  //              4L, "gerTyp 4", "gerOrt 4", "01.04.2022", "aktenzeichen 4", "ABCDE20220001"));
  //      previousDecisionsListInDB.add(
  //          new PreviousDecision(
  //              5L, "gerTyp 5", "gerOrt 5", "01.05.2022", "aktenzeichen 5", "ABCDE20220001"));
  //      count = Long.valueOf(previousDecisionsListInDB.size());
  //    }
  //
  //    @Test
  //    void testGetByDocumentnumberWithPreviousDecisions() {
  //      when(repository.findByDocumentnumber(documentNr))
  //          .thenReturn(Mono.just(DocumentUnitDTO.EMPTY));
  //      when(previousDecisionRepository.findAllByDocumentnumber(documentNr))
  //          .thenReturn(Flux.fromIterable(previousDecisionsListInDB));
  //      StepVerifier.create(service.getByDocumentnumber(documentNr))
  //          .consumeNextWith(
  //              documentUnit -> {
  //                assertEquals(documentUnit.previousDecisions().size(), count);
  //                PreviousDecision previousDecision = documentUnit.previousDecisions().get(0);
  //                assertEquals(1L, previousDecision.id);
  //                assertEquals("gerOrt 1", previousDecision.courtPlace);
  //                assertEquals("gerTyp 1", previousDecision.courtType);
  //                assertEquals("01.01.2022", previousDecision.date);
  //                assertEquals("aktenzeichen 1", previousDecision.fileNumber);
  //                assertEquals(documentNr, previousDecision.documentnumber);
  //              })
  //          .verifyComplete();
  //      verify(repository).findByDocumentnumber("ABCDE20220001");
  //    }
  //
  //    @Test
  //    void testUpdateDocumentUnitWithPreviousDecisionsDelete() {
  //      previousDecisionsIdsToDelete.add("2");
  //      previousDecisionsIdsToDelete.add("4");
  //      inputPreviousDecisionFromFE =
  //          previousDecisionsListInDB.stream()
  //              .filter(
  //                  previousDecision ->
  //                      !previousDecisionsIdsToDelete.contains(previousDecision.id.toString()))
  //              .toList();
  //      DocumentUnitObj documentUnitObj = setUpMockDBQueries();
  //
  //      StepVerifier.create(service.updateDocumentUnit(documentUnitObj.documentUnit()))
  //          .consumeNextWith(
  //              documentUnit -> {
  //                assertEquals(inputPreviousDecisionFromFE.size(),
  // previousDecisionsListInDB.size());
  //                assertEquals(
  //                    inputPreviousDecisionFromFE.size(),
  // documentUnit.previousDecisions().size());
  //                assertTrue(
  //                    documentUnit.previousDecisions().containsAll(inputPreviousDecisionFromFE));
  //                assertTrue(previousDecisionsListInDB.containsAll(inputPreviousDecisionFromFE));
  //                assertEquals(3, previousDecisionsListInDB.size());
  //                List<Long> remainIds = getRemainsIds();
  //                assertTrue(remainIds.contains(1L));
  //                assertTrue(remainIds.contains(3L));
  //                assertTrue(remainIds.contains(5L));
  //                assertFalse(remainIds.contains(2L));
  //                assertFalse(remainIds.contains(4L));
  //                assertEquals(documentUnit, documentUnitObj.documentUnit());
  //              })
  //          .verifyComplete();
  //      verify(repository).save(documentUnitObj.documentUnitDTO());
  //    }
  //
  //    @Test
  //    void testUpdateDocumentUnitWithPreviousDecisionsInsert() {
  //      inputPreviousDecisionFromFE = new ArrayList<>(previousDecisionsListInDB);
  //      inputPreviousDecisionFromFE.add(
  //          new PreviousDecision(
  //              null, "gerTyp 6", "gerOrt 6", "01.01.2022", "aktenzeichen 6", documentNr));
  //      inputPreviousDecisionFromFE.add(
  //          new PreviousDecision(
  //              null, "gerTyp 7", "gerOrt 7", "01.01.2022", "aktenzeichen 7", documentNr));
  //      DocumentUnitObj documentUnitObj = setUpMockDBQueries();
  //
  //      StepVerifier.create(service.updateDocumentUnit(documentUnitObj.documentUnit()))
  //          .consumeNextWith(
  //              documentUnit -> {
  //                assertEquals(inputPreviousDecisionFromFE.size(),
  // previousDecisionsListInDB.size());
  //                assertEquals(
  //                    inputPreviousDecisionFromFE.size(),
  // documentUnit.previousDecisions().size());
  //                assertTrue(
  //                    documentUnit.previousDecisions().containsAll(inputPreviousDecisionFromFE));
  //                assertTrue(previousDecisionsListInDB.containsAll(inputPreviousDecisionFromFE));
  //                List<Long> remainIds = getRemainsIds();
  //                assertTrue(remainIds.contains(6L));
  //                assertTrue(remainIds.contains(7L));
  //                assertEquals(documentUnit, documentUnitObj.documentUnit());
  //              })
  //          .verifyComplete();
  //      verify(repository).save(documentUnitObj.documentUnitDTO());
  //    }
  //
  //    @Test
  //    void testUpdateDocumentUnitWithPreviousDecisionsUpdate() {
  //      inputPreviousDecisionFromFE = new ArrayList<>(previousDecisionsListInDB);
  //      inputPreviousDecisionFromFE.get(0).courtPlace = "new gerOrt";
  //      inputPreviousDecisionFromFE.get(0).courtType = "new gerTyp";
  //      inputPreviousDecisionFromFE.get(0).date = "30.01.2022";
  //      inputPreviousDecisionFromFE.get(0).fileNumber = "new fileNumber";
  //
  //      DocumentUnitObj documentUnitObj = setUpMockDBQueries();
  //
  //      StepVerifier.create(service.updateDocumentUnit(documentUnitObj.documentUnit()))
  //          .consumeNextWith(
  //              documentUnit -> {
  //                assertEquals(inputPreviousDecisionFromFE.size(),
  // previousDecisionsListInDB.size());
  //                assertEquals(
  //                    inputPreviousDecisionFromFE.size(),
  // documentUnit.previousDecisions().size());
  //                assertTrue(
  //                    documentUnit.previousDecisions().containsAll(inputPreviousDecisionFromFE));
  //                assertTrue(previousDecisionsListInDB.containsAll(inputPreviousDecisionFromFE));
  //                assertEquals(
  //                    new PreviousDecision(
  //                        1L, "new gerTyp", "new gerOrt", "30.01.2022", "new fileNumber",
  // documentNr),
  //                    documentUnit.previousDecisions().get(0));
  //                assertEquals(documentUnit, documentUnitObj.documentUnit());
  //              })
  //          .verifyComplete();
  //      verify(repository).save(documentUnitObj.documentUnitDTO());
  //    }
  //
  //    @Test
  //    void testUpdateDocumentUnitWithPreviousDecisionsInsertUpdateDelete() {
  //      previousDecisionsIdsToDelete.add("2");
  //      previousDecisionsIdsToDelete.add("4");
  //      inputPreviousDecisionFromFE =
  //          new ArrayList<>(
  //              previousDecisionsListInDB.stream()
  //                  .filter(
  //                      previousDecision ->
  //
  // !previousDecisionsIdsToDelete.contains(previousDecision.id.toString()))
  //                  .toList());
  //      inputPreviousDecisionFromFE.get(0).courtPlace = "new gerOrt";
  //      inputPreviousDecisionFromFE.get(0).courtType = "new gerTyp";
  //      inputPreviousDecisionFromFE.get(0).date = "30.01.2022";
  //      inputPreviousDecisionFromFE.get(0).fileNumber = "new fileNumber";
  //      inputPreviousDecisionFromFE.add(
  //          new PreviousDecision(
  //              null, "gerTyp 6", "gerOrt 6", "01.01.2022", "aktenzeichen 6", documentNr));
  //      inputPreviousDecisionFromFE.add(
  //          new PreviousDecision(
  //              null, "gerTyp 7", "gerOrt 7", "01.01.2022", "aktenzeichen 7", documentNr));
  //
  //      DocumentUnitObj documentUnitObj = setUpMockDBQueries();
  //
  //      StepVerifier.create(service.updateDocumentUnit(documentUnitObj.documentUnit()))
  //          .consumeNextWith(
  //              documentUnit -> {
  //                assertEquals(inputPreviousDecisionFromFE.size(),
  // previousDecisionsListInDB.size());
  //                assertEquals(
  //                    inputPreviousDecisionFromFE.size(),
  // documentUnit.previousDecisions().size());
  //                assertTrue(
  //                    documentUnit.previousDecisions().containsAll(inputPreviousDecisionFromFE));
  //                assertTrue(previousDecisionsListInDB.containsAll(inputPreviousDecisionFromFE));
  //                assertEquals(5, previousDecisionsListInDB.size());
  //                List<Long> remainIds = getRemainsIds();
  //                assertTrue(remainIds.contains(1L));
  //                assertTrue(remainIds.contains(3L));
  //                assertTrue(remainIds.contains(5L));
  //                assertTrue(remainIds.contains(6L));
  //                assertTrue(remainIds.contains(7L));
  //                assertFalse(remainIds.contains(2L));
  //                assertFalse(remainIds.contains(4L));
  //                assertEquals(
  //                    new PreviousDecision(
  //                        1L, "new gerTyp", "new gerOrt", "30.01.2022", "new fileNumber",
  // documentNr),
  //                    documentUnit.previousDecisions().get(0));
  //                assertEquals(documentUnit, documentUnitObj.documentUnit());
  //              })
  //          .verifyComplete();
  //      verify(repository).save(documentUnitObj.documentUnitDTO());
  //    }
  //  }

  @Test
  void testDeleteByUuid_withoutFileAttached() {
    // I think I shouldn't have to insert a specific DocumentUnit object here?
    // But if I don't, the test by itself succeeds, but fails if all tests in this class run
    // something flaky with the repository mock? Investigate this later
    DocumentUnit documentUnit = DocumentUnit.builder().uuid(TEST_UUID).build();
    // can we also test that the fileUuid from the DocumentUnit is used? with a captor somehow?
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnit));
    when(repository.delete(any(DocumentUnit.class))).thenReturn(Mono.just(mock(Void.class)));

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            string -> {
              assertNotNull(string);
              assertEquals("done", string);
            })
        .verifyComplete();

    verify(s3AsyncClient, times(0)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withFileAttached() {
    DocumentUnit documentUnit =
        DocumentUnit.builder().uuid(TEST_UUID).s3path(TEST_UUID.toString()).build();
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnit));
    when(repository.delete(any(DocumentUnit.class))).thenReturn(Mono.just(mock(Void.class)));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            string -> {
              assertNotNull(string);
              assertEquals("done", string);
            })
        .verifyComplete();

    verify(s3AsyncClient, times(1)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromBucket() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocumentUnit.EMPTY));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenThrow(SdkException.create("exception", null));

    StepVerifier.create(service.deleteByUuid(TEST_UUID)).expectError().verify();

    verify(repository).findByUuid(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocumentUnit.EMPTY));
    doThrow(new IllegalArgumentException()).when(repository).delete(DocumentUnit.EMPTY);

    StepVerifier.create(service.deleteByUuid(TEST_UUID)).expectError().verify();

    verify(repository).findByUuid(TEST_UUID);
  }

  @Test
  void testUpdateDocumentUnit() {
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .creationtimestamp(Instant.now())
            .fileuploadtimestamp(Instant.now())
            .previousDecisions(null)
            .build();
    var documentUnitDTO = DocumentUnitTransformer.generateDTO(documentUnit);
    when(repository.save(documentUnit)).thenReturn(Mono.just(documentUnit));
    StepVerifier.create(service.updateDocumentUnit(documentUnit))
        .consumeNextWith(du -> assertEquals(du, documentUnit))
        .verifyComplete();
    verify(repository).save(documentUnit);
  }

  @Test
  void testPublishByEmail() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocumentUnit.EMPTY));
    XmlMail xmlMail =
        new XmlMail(
            TEST_UUID,
            "receiver address",
            "subject",
            "xml",
            "200",
            List.of("status messages"),
            "filename",
            null);
    when(publishService.publish(DocumentUnit.EMPTY, RECEIVER_ADDRESS))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));
    StepVerifier.create(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS))
        .consumeNextWith(
            mailResponse ->
                assertThat(mailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).publish(DocumentUnit.EMPTY, RECEIVER_ADDRESS);
  }

  @Test
  void testPublishByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.empty());

    StepVerifier.create(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS)).verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService, never()).publish(DocumentUnit.EMPTY, RECEIVER_ADDRESS);
  }

  @Test
  void testGetLastPublishedXmlMail() {
    XmlMail xmlMail =
        new XmlMail(
            TEST_UUID,
            "receiver address",
            "subject",
            "xml",
            "200",
            List.of("message"),
            "filename",
            null);
    when(publishService.getLastPublishedXml(TEST_UUID))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));

    StepVerifier.create(service.getLastPublishedXmlMail(TEST_UUID))
        .consumeNextWith(
            xmlMailResponse ->
                assertThat(xmlMailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(publishService).getLastPublishedXml(TEST_UUID);
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
