package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
class JurisXmlExporterWrapperIntegrationTest {

  private JurisXmlExporter jurisXmlExporter;
  private String encryptedXml;
  private ObjectMapper objectMapper;
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String documentNr = "ABCDE20220001";

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Test
  void test_validDocumentUnit_shouldReturnEncryptedXMLString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of("fileNumber1", "fileNumber2"))
            .deviatingFileNumbers(List.of("deviatingFileNumber1", "deviatingFileNumber2"))
            .court(new Court(UUID.randomUUID(), "courtType", null, null, null))
            .ecli("ecli")
            .deviatingEclis(List.of("dev-ecli-1", "dev-ecli-2"))
            .documentType(
                DocumentType.builder().jurisShortcut("category").label("category123").build())
            .ecli("ecli")
            .decisionDate(LocalDate.parse("2021-01-01"))
            .deviatingDecisionDates(
                List.of(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-02")))
            .build();

    Texts texts = Texts.builder().decisionName("decisionName").build();

    List<PreviousDecision> previousDecisions =
        List.of(
            PreviousDecision.builder()
                .uuid(UUID.randomUUID())
                .court(new Court(UUID.randomUUID(), "courtType", "courtPlace", "courtLabel", null))
                .decisionDate(LocalDate.parse("2020-05-06"))
                .dateKnown(true)
                .fileNumber("fileNumber")
                .documentType(
                    DocumentType.builder().jurisShortcut("category").label("category123").build())
                .build());

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .coreData(coreData)
            //            .proceedingDecisions(proceedingDecisions)
            .texts(texts)
            .build();

    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals(
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDvFsYIsa4TFtD4nBC6ZR297NQ/41IN2doHsIhqWCKBcTx0hqrZ2tc2+YkjVfM9f2XO35eW7D7FAmeIwZH5t+Gbew6rdridX/YU08pl3QuT9WS47sNnx2I5sGzbaxh/U7OBnnSruE3d0l/CLqBeJEKYVJA7juGg/v1fZoAewuwkW9lDNSPlA7U87Q3xO6W7xy2ZGMH0njO6xcIrWcfFF3gEOmdqXPEAr6llMTKYdhc9Q//NYgCzMz+LyN9OxQaI6joALZczrv8mcHT3z1AJp/9wGU25TqhS/lTpopeRqeANVhF9qRIJ5if2QF2hwpMpi859+j7dBkNlplokUKhby1vqb127HWF4FfJ69oPTYLUhpyMQ7dvebmnTBMzNmbBgz2VvKmyrlDcSq58fdFK/wzy5qdqT6CIy4yCRlkKjjM9f5o3b3iOSXgEKLm4EmsDMHH1eMNnOnSKwmFTjDa8EiPBoHeXwTGMKHWksQNpemscNj9Lq9+4mkekWtmDq8d+qsj4SUgDe2wiba6h7+2cvdIkLsNgdQI7jqTAaiFpgMDfT9tTvtE3ANfapYOZdWcGUnhjOQ6HQHn8x9IozSJ1SWd146+VN++rElOnhqeeGKmKkD98Mht8QEd2ROA73sP+t7rP9M5iqp9ie8gzr7wTkPvrpAbJWyqp/oR/DmKKS8mcFYmezonOlwt/QP8kKmjX9KKmfInAE1GR/MhzkiRegS2qu9txX81QJM5SgYOiDutFow6OcVsj4VKD5IXhNba4nzt3EjiTYs8AImLAnjmb8dbOMWrEWP+C7fWH//lhcVFwrJMOhILbAQO6eNy/UmtVvzQm3EmUfYmd6KVhrrLwHQ8qGKm9szbgrQ+MiQ5n4QQXrUXUzBVDlkLuH0YpyGG7WVLUS0VJooomjlqbIJS/l4zdngTUm2lLzmLnEKyANIElsgP9zyNt5GI8nLZ8vrbvUDWa9RTmb29ronLHuBiJsXVeMG9GaUUpPiFoH7ZGhDVDhi5/5zyc+IzRkWk3u2rtbGRSTsdekPhHMmOdqZteK/zaESFq5iV2pJl4ahwfk4UYdjzxgZfqmYu2hUSihbtzLWvm2tynT8bPnD1bUw7ggn1LWF1DlE1org2HS3Nd3j8hTVsCpvV2MRJT4r2dSRLd3fP7kP5Ph1Lt2Z3gT5FYL8mqceszMEACnydLT0CePZrYgYVopEIjdgA98by/b1LxhsMFzEmUfYmd6KVhrrLwHQ8qGKaUIIg0c+HeXHV1Wmlv3QrWLo4IDtYb1NZqy6rvUpbnNIxMkdnlpy8vEkrk6C5zWRuBdubP1nBwZk5ToMHQ5kvf0aLke9Ugx/FS3WpWZC2jhcP08JuBZayJrQ0Pt+ioqAMIUVgKAnSgS99mF0l49jhqJIO7intDUDbKFrQ7GZuVXLlR2PUOtC7wlwkkgFq+lhrdQAeCoSaQL6dhy6balJfkEDo4twezrxcvbV+MBHOOUfSwk0hFkPR6JsUSPM/3tutpEZ/8I3ZbxXwJ4AqcI3QME6vj9iP7D2qQc4SljY00WZ49C+Chtfd7vG/gXepm0Eu37CpBVg7tfBzbwevHOO/h4RHaJzXDnxpuLpGF//Lp1nq0lA6Pl+yCv5vQxXBATD1hks1/QLDIU7AHh/nAF9XJDodAefzH0ijNInVJZ3XjonVQ81Fh6+j1EJ0PtlkVRg0nccJNnufHx1qmIVzc0tq8ShYwsMCieqM6KNwz+1agG1ExCVKxeRHsevT565ySuXvr06iJyy493+EnSfhErBZw==",
        encryptedXml);
  }

  @Test
  void test_invalidDocumentUnitMissingMandatoryFields_shouldReturnEmptyString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .build();

    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals("", encryptedXml);
  }

  // In order to be in sync with the model, all possible fields are set in this
  // test documentUnit and checked for null fields. Everytime a new field is
  // added to the data model this test will fail, as it is not set in the test
  // documentUnit. The test documentUnit needs to be updated accordingly to be
  // in sync with the model.
  @Test
  void testDocumentUnitIsSyncedWithModel()
      throws NoSuchFieldException,
          SecurityException,
          IllegalArgumentException,
          IllegalAccessException {
    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of("fileNumber"))
            .documentType(DocumentType.builder().jurisShortcut("ca").label("category").build())
            .procedure(Procedure.builder().label("procedure").build())
            .ecli("ecli")
            .deviatingEclis(List.of("dev-ecli-1", "dev-ecli-2"))
            .appraisalBody("appraisalBody")
            .decisionDate(LocalDate.parse("2021-01-01"))
            .legalEffect("legalEffect")
            .inputTypes(List.of("inputType"))
            .documentationOffice(DocumentationOffice.builder().abbreviation("fooOffice").build())
            .region("region")
            .build();
    Texts texts =
        Texts.builder()
            .decisionName("decisionName")
            .headline("headline")
            .guidingPrinciple("guidingPrinciple")
            .headnote("headnote")
            .tenor("tenor")
            .reasons("reasons")
            .caseFacts("caseFacts")
            .decisionReasons("decisionReasons")
            .build();

    List<PreviousDecision> previousDecisions =
        List.of(
            PreviousDecision.builder()
                .uuid(UUID.randomUUID())
                .documentNumber("documentNumber")
                .uuid(UUID.randomUUID())
                .court(new Court(UUID.randomUUID(), "courtType", "courtPlace", "courtLabel", null))
                .decisionDate(LocalDate.parse("2020-04-05"))
                .dateKnown(true)
                .fileNumber("fileNumber")
                .deviatingFileNumber("deviatingFileNumber")
                .documentType(
                    DocumentType.builder().jurisShortcut("category").label("category123").build())
                .build());

    ContentRelatedIndexing indexing =
        ContentRelatedIndexing.builder()
            .keywords(List.of("keyword1", "keyword2"))
            .fieldsOfLaw(
                List.of(
                    FieldOfLaw.builder()
                        .id(UUID.randomUUID())
                        .identifier("SF-01")
                        .text("field of law text")
                        .build()))
            .norms(List.of(NormReference.builder().singleNorm("01").build()))
            .build();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .dataSource(DataSource.NEURIS)
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .coreData(coreData)
            .previousDecisions(previousDecisions)
            .ensuingDecisions(new ArrayList<>())
            .texts(texts)
            .contentRelatedIndexing(indexing)
            .status(Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build())
            .build();

    assertThat(documentUnit).hasNoNullFieldsOrProperties();

    for (Field field : documentUnit.getClass().getDeclaredFields()) {
      if (field.getType().equals(CoreData.class) || field.getType().equals(Texts.class))
        assertThat(field).hasNoNullFieldsOrProperties();
      if (field.getType().equals(List.class)) {
        field.setAccessible(true);
        List<PreviousDecision> previousDecisionsList =
            (List<PreviousDecision>) field.get(documentUnit);
        for (PreviousDecision previousDecision : previousDecisionsList) {
          assertThat(previousDecision).hasNoNullFieldsOrProperties();
        }
      }
    }
  }
}
