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
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
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

    List<PreviousDecision> previousDecisions =
        List.of(
            PreviousDecision.builder()
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .date(Instant.parse("2020-05-06T00:00:00Z"))
                .fileNumber("fileNumber")
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
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDsQUfTAw9rNE1esKkprKcKLZtd7IHfw4GJydedmPZLTdvj8gtBupPi+gOTh5k2GBNPDFWvJwQdNr4HGDgrNGnw7S/LQVrpamcxw6Ld4+KeqvUMznaj7uQmQMvpBIWdiGsDL+VUAR2W0HKvtrB3d48dOgcXcVHluk8Cc+1kozOELjc0L3GREwFdVjdFbOOGrbnf6+ZwxhX2f6YV8X02vDLzeN0Ax7+N+bKzf3Dz/yjmouLlkQT9+gjKJglpSRhzWe7rsvV1K50xosnzeS3CdRUsZ+D6/qIKicEwbGS1hMNijtv0TheBJQGoiq4Zm0Ai6i/b/BYF7oS7+IkxWu5w0GS6TJFJkDn7lGBtHU8HbI326vlneD9E3/21jknq36RslkKA6I/PoV2jy16mVlSS8HalgQIOVmZArRsp8Sjihhi2rSBELJeJvDj5Lf6PaeFDQwZGwTAxfxTba0yY/a6VVh9tSqoP1cVawvm/H4Qoy4Bo4hflm/l7S0hlKCl/KKkFudXvOasJG6rwI8VMOCjPtz+hZ47A3dpx8v2fkwurOrx/4niCAaaV7fqGDlpLxt+ZH0TEiRJx7EiBwMeiKKiq5EB/jzIzlZ6b0uP53iL//9LxbiRhWFJb7qTPvTbslfAXMOeBknwjHmbCPMl5eMUxaQyx4hkVljkwyEGU/wPS5CTZp1TDnoAkIoPgEpLvI8HRM0HXVRwYyDJFCIbO0lG9TMBLKB4ZNbfKjpsKoYgQJb8a5++Adxhp6J8zJ+Eg2nKCLl7hDwZc92KH2idh6EknbrLt77HXpD4RzJjnambXiv82hEhauYldqSZeGocH5OFGHY8/NVtAJTocxQj6cE7rat0P+XJ9xFF0Sh825n/aUnHZNz9Q5RNaK4Nh0tzXd4/IU1bCJh7YhD/SG0/AezRW2lIYghO6C1ZsItAihBoLwUMVDAVw/Twm4FlrImtDQ+36KioAwhRWAoCdKBL32YXSXj2OGokg7uKe0NQNsoWtDsZm5VcuVHY9Q60LvCXCSSAWr6WHtW35Zq6me+WXawyHBfn1pSMTJHZ5acvLxJK5Oguc1kVMl2i28RP1WwkdZL+VOIMB3Ufzgg05ITagCx1fBE43iYbr1QuXwoWpbFv1Nx3r0j5a7dJBqnQkAtzLKQqNH1JBcP08JuBZayJrQ0Pt+ioqAMIUVgKAnSgS99mF0l49jhqJIO7intDUDbKFrQ7GZuVXLlR2PUOtC7wlwkkgFq+lhuKVmodLHl6vZbsRaijqh2U1JtpS85i5xCsgDSBJbID/c8jbeRiPJy2fL6271A1mv72Xyq3P5J14kHnKSGrlsDoIuFcUUAz3886v7EcJNOj6zMwQAKfJ0tPQJ49mtiBhWikQiN2AD3xvL9vUvGGwwXMSZR9iZ3opWGusvAdDyoYppQgiDRz4d5cdXVaaW/dCtdDQfTTeuPKVxCPjHZEtuRcP/L/cveFjbEuC5jdTjo9FBOOU4a9lEwtXwdUdlbvPDPC/k2AIs6ngFe6wrH9tMNWwegjorC97PsR+fsfG4O0CqurRV0FnHIJUEh2v9thZJz8GckHmpifuuxBufv7pV8dvp9743xNxQDfzspXgUQ1Mw8y6uevPhKnDi9Eg0HHnE",
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
  public void testDocumentUnitIsSyncedWithModel()
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

    List<PreviousDecision> previousDecisions =
        List.of(
            PreviousDecision.builder()
                .id(1L)
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .date(Instant.parse("2020-04-05T00:00:00Z"))
                .fileNumber("fileNumber")
                .build());

    ContentRelatedIndexing indexing =
        new ContentRelatedIndexing(
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
            .previousDecisions(previousDecisions)
            .texts(texts)
            .contentRelatedIndexing(indexing)
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
