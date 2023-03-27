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
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
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
public class JurisXmlExporterWrapperIntegrationTest {

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
                .date(Instant.parse("2020-05-06T00:00:00Z"))
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
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDsQUfTAw9rNE1esKkprKcKLZtd7IHfw4GJydedmPZLTdvj8gtBupPi+gOTh5k2GBNPDFWvJwQdNr4HGDgrNGnw7S/LQVrpamcxw6Ld4+KeqvUMznaj7uQmQMvpBIWdiGsDL+VUAR2W0HKvtrB3d48dOgcXcVHluk8Cc+1kozOELjc0L3GREwFdVjdFbOOGrbnf6+ZwxhX2f6YV8X02vDLzeN0Ax7+N+bKzf3Dz/yjmouLlkQT9+gjKJglpSRhzWe7rsvV1K50xosnzeS3CdRUsZ+D6/qIKicEwbGS1hMNijtv0TheBJQGoiq4Zm0Ai6i/b/BYF7oS7+IkxWu5w0GS6TJFJkDn7lGBtHU8HbI326vlneD9E3/21jknq36RslkKA6I/PoV2jy16mVlSS8HalgpzCMlWx7MWLopibE5YpZOFJ4cQeihoAOJyfPcUpE68Y5i2mcIiCuU1v0LAei3xqTMNmx3IHBzDAqamx7xgGWnpIUi4M4dn6ms+RDzCqIirfZrXpOXYZG4ZZ9owHLFz12Er43hcr0Qgo9ik6IY52Tn/H6jUUqfc4mzmNX9Zmns/Vj/GZYDVQm6SEEveMFn6qhy9vs+v7TM2YJQHcDUq/bbZUXSwNdFQ6onpXUPmku39x5lmA60rGEuacfCKSQHFOMhG/qo0aM0CWR9lLwUKUmdXGswZ/+GVD96b3zJmzOZzjD/y/3L3hY2xLguY3U46PRnsNwAVSeUakkfuAGXxbzYCBkY5GtZY5BB2x5qnirjgfB8dawwO4jaWflseO+j6kFETrz9cRD09y74hiENAdllMvb7Pr+0zNmCUB3A1Kv222VF0sDXRUOqJ6V1D5pLt/ceZZgOtKxhLmnHwikkBxTjIRv6qNGjNAlkfZS8FClJnUgqlLWiFFUJh1S/61kre3UQQOji3B7OvFy9tX4wEc45WBebVaRh8qVliFkvVv+z9rVxzh3vbqppFX14GS8uWfhBupQPgf/n/Qna13TQhMQ7/PLAsC1M1YfQ+Uaj8UlguZ8Yk593JXgRBM7gVjVKmGBmePQvgobX3e7xv4F3qZtBAyLivCBSBaLWq+URLeMtNxybdVsoGWqQ8CbUnc53ik0TUm2lLzmLnEKyANIElsgPwKGx95DQsrhiNV+LgE64hutC3RFeuEHCSXrx+PF/USqzLllH+Q6APJ1GbQRqbLmkbMzBAAp8nS09Anj2a2IGFZrQvJkZP1XKCDKkSDI2fCv5qFclkMWCb6QyX6YAuEM8KwAVd5YXWr2MDgn9kjgeRI=",
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
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
          IllegalAccessException {
    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of("fileNumber"))
            .documentType(DocumentType.builder().jurisShortcut("ca").label("category").build())
            .procedure("procedure")
            .ecli("ecli")
            .deviatingEclis(List.of("dev-ecli-1", "dev-ecli-2"))
            .appraisalBody("appraisalBody")
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
            .legalEffect("legalEffect")
            .inputType("inputType")
            .center("center")
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
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .date(Instant.parse("2020-04-05T00:00:00Z"))
                .fileNumber("fileNumber")
                .documentType(
                    DocumentType.builder().jurisShortcut("category").label("category123").build())
                .build());

    ContentRelatedIndexing indexing =
        new ContentRelatedIndexing(
            List.of("keyword1", "keyword2"),
            List.of(
                FieldOfLaw.builder().id(1L).identifier("SF-01").text("field of law text").build()));

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
