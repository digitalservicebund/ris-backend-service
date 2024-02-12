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
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDuP03l1cHT01qPDsz5/YdGLbk83I/q39TAHzEk98dfqP6L5Je85I1A8Nv3cOvUlLI3mW6FeNP/t7qTuPepH436mGUlG1DyYg422X4BbSMfFbPDfYGKunu4cDIuV+1RWsLrl59yaE7KFPfT8ROebs/01khBFTEVVeOEUXFo74YOzoAlWz0dkUufAOP/Abv5tQQ6wbdET4eW2tDQbWY2+yLpk1CrRLVIaPd+ru9K78Rl5ENXLjkdD7K4xHFB5YNoUsMQjyB38ds4T+D0uG86XzmEee/lNwjRFd5t7fWTpFirBK1xSEvPE7/rmVdbLHcksvT9fTCyNY15lAhpeTCRvdQfoNUTyt5z/VClF7hmnkqIF0kxQSXPT0GuCN9YQK+hsNHdNALFu7v5ZfFX0rk0r+5PomNcqATDiQ+AOwFpemL5L4a1EZUdJs842KvswLkcXPcWMHinWLJNms2NhbrBnHobTbJWyqp/oR/DmKKS8mcFYmezonOlwt/QP8kKmjX9KKmcGEaPeUrTky8p/PbcZ+o1ViZKoD0waTRytBWcGeg4t9NYwp7ahEZ5CEYrAwEdkImDS7zOORRKDiDOv16+Gj1QzQEW7U9QJZqB/se+UDvgF7f5zyc+IzRkWk3u2rtbGRSTsdekPhHMmOdqZteK/zaESFq5iV2pJl4ahwfk4UYdjzxgZfqmYu2hUSihbtzLWvm2JhlTTWGF0n+zryrrXLaVGOvlosCnolBK+mixwI2GMnDEyVmg3QW0LquuFeTgeYmsAZDePELlRIU2niLuevlwL0nccJNnufHx1qmIVzc0tq8ShYwsMCieqM6KNwz+1agGDsn4sr0FU7DB6FIgqi4kjlKbN2N5aPnTqtaKYznrxHw+fsmm3KmiHZPzLsmXh18u8i/NIrkmJhjdEktIg8iRSkOh0B5/MfSKM0idUlndeOmuFAazwwA5ENyCUBm6+j1GWu3SQap0JALcyykKjR9SQXD9PCbgWWsia0ND7foqKgDCFFYCgJ0oEvfZhdJePY4aiSDu4p7Q1A2yha0OxmblVy5Udj1DrQu8JcJJIBavpYbilZqHSx5er2W7EWoo6odlNSbaUvOYucQrIA0gSWyA/3PI23kYjyctny+tu9QNZr4z4BbN+7WSOHyyOnNA9v2voSC2wEDunjcv1JrVb80JtxJlH2JneilYa6y8B0PKhipvbM24K0PjIkOZ+EEF61F1MwVQ5ZC7h9GKchhu1lS1E9PeXQQ1UbQxbMcv4JF92lWJigNKvPb3/cmtoJnhKlIR3wtMvvJKlqcIdcMdZUfyvg5KPaJZpqfbapc0qltiCbMJma0G2I7viADuHOj5kJUOiSDu4p7Q1A2yha0OxmblVur37iaR6Ra2YOrx36qyPhJSAN7bCJtrqHv7Zy90iQuw5OeArIu5CjlhExzjnfW3/44/vjEoLRUKU1CWj1PgIxnkajLfG/Lhy1ETf4lkhlmjc0JvgLQ/0U6ThP3J1J7wt/7b1xhGmwJNBbp3jeRlQFjLEzunzgKXV/ESYGFitb/7J4IVtC5limVJh6fuPU0ZBErbvdkTy5DmCzjl7FX8/ciuP197UGgxcN8vFinnnmQyKOY0jZ+8Wr+mGpAEgxvOu1jCntqERnkIRisDAR2QiYMsxMgZMhJGnEDH2CNyKvK5sufJc5Vm2n/kqyzBPOnXz7ZZriLuhzoUMCnYwEqZ2PwhGQhEsCE/XulYEM78OM/GJr7Py0mlT+qxDCk/hnHKni2qoy89/4iun8FDvayaUsA==",
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
