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
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
            .leadingDecisionNormReferences(List.of("BGB §1"))
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
            .previousDecisions(previousDecisions)
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
            //            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            //            .s3path("s3path")
            //            .filetype("filetype")
            //            .filename("filename")
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
      throws SecurityException, IllegalArgumentException, IllegalAccessException {
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
            .leadingDecisionNormReferences(List.of("BGB", "ZPO", "StPO", "GG"))
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
            .norms(
                List.of(
                    NormReference.builder()
                        .singleNorms(List.of(SingleNorm.builder().singleNorm("01").build()))
                        .build()))
            .build();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .originalFiles(Collections.emptyList())
            .dataSource(DataSource.NEURIS)
            .coreData(coreData)
            .previousDecisions(previousDecisions)
            .ensuingDecisions(new ArrayList<>())
            .texts(texts)
            .borderNumbers(Collections.emptyList())
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
