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
