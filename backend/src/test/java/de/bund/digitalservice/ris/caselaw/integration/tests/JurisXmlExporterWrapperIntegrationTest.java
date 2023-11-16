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
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.lang.reflect.Field;
import java.time.Instant;
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
            .court(new Court("courtType", null, null, null))
            .ecli("ecli")
            .deviatingEclis(List.of("dev-ecli-1", "dev-ecli-2"))
            .documentType(
                DocumentType.builder().jurisShortcut("category").label("category123").build())
            .ecli("ecli")
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
            .deviatingDecisionDates(
                List.of(
                    Instant.parse("2021-01-01T00:00:00Z"), Instant.parse("2021-01-02T00:00:00Z")))
            .build();

    Texts texts = Texts.builder().decisionName("decisionName").build();

    List<ProceedingDecision> proceedingDecisions =
        List.of(
            ProceedingDecision.builder()
                .uuid(UUID.randomUUID())
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .decisionDate(Instant.parse("2020-05-06T00:00:00Z"))
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
            .proceedingDecisions(proceedingDecisions)
            .texts(texts)
            .build();

    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals(
        "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",
        encryptedXml);
  }

  @Test
  void test_invalidDocumentUnitMissingMandatoryFields_shouldReturnEmptyString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .creationtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
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
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
            .legalEffect("legalEffect")
            .inputType("inputType")
            .documentationOffice(
                DocumentationOffice.builder().label("fooOffice").label("FO").build())
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

    List<ProceedingDecision> proceedingDecisions =
        List.of(
            ProceedingDecision.builder()
                .uuid(UUID.randomUUID())
                .documentNumber("documentNumber")
                .dataSource(DataSource.NEURIS)
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .decisionDate(Instant.parse("2020-04-05T00:00:00Z"))
                .dateKnown(true)
                .fileNumber("fileNumber")
                .documentType(
                    DocumentType.builder().jurisShortcut("category").label("category123").build())
                .build());

    ContentRelatedIndexing indexing =
        ContentRelatedIndexing.builder()
            .keywords(List.of("keyword1", "keyword2"))
            .fieldsOfLaw(
                List.of(
                    FieldOfLaw.builder()
                        .id(1L)
                        .identifier("SF-01")
                        .text("field of law text")
                        .build()))
            .norms(List.of(DocumentUnitNorm.builder().singleNorm("01").build()))
            .build();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .creationtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .dataSource(DataSource.NEURIS)
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .coreData(coreData)
            .proceedingDecisions(proceedingDecisions)
            .texts(texts)
            .contentRelatedIndexing(indexing)
            .status(
                DocumentUnitStatus.builder()
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .build())
            .build();

    assertThat(documentUnit).hasNoNullFieldsOrProperties();

    for (Field field : documentUnit.getClass().getDeclaredFields()) {
      if (field.getType().equals(CoreData.class) || field.getType().equals(Texts.class))
        assertThat(field).hasNoNullFieldsOrProperties();
      if (field.getType().equals(List.class)) {
        field.setAccessible(true);
        List<ProceedingDecision> previousDecisionsList =
            (List<ProceedingDecision>) field.get(documentUnit);
        for (ProceedingDecision proceedingDecision : previousDecisionsList) {
          assertThat(proceedingDecision).hasNoNullFieldsOrProperties();
        }
      }
    }
  }
}
