package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class JurisXmlExporterWrapperTest {

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
  @Tag("IntegrationTest")
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
                .courtType("courtType")
                .courtPlace("courtPlace")
                .date("date")
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
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDsQUfTAw9rNE1esKkprKcKLZtd7IHfw4GJydedmPZLTdvj8gtBupPi+gOTh5k2GBNPDFWvJwQdNr4HGDgrNGnw7S/LQVrpamcxw6Ld4+KeqvUMznaj7uQmQMvpBIWdiGsDL+VUAR2W0HKvtrB3d48dOgcXcVHluk8Cc+1kozOELjc0L3GREwFdVjdFbOOGrbnf6+ZwxhX2f6YV8X02vDLzeN0Ax7+N+bKzf3Dz/yjmouLlkQT9+gjKJglpSRhzWe7rsvV1K50xosnzeS3CdRUsZ+D6/qIKicEwbGS1hMNijtv0TheBJQGoiq4Zm0Ai6i/b/BYF7oS7+IkxWu5w0GS6TJFJkDn7lGBtHU8HbI326vlneD9E3/21jknq36RslkKA6I/PoV2jy16mVlSS8HalgQIOVmZArRsp8Sjihhi2rSBELJeJvDj5Lf6PaeFDQwZGwTAxfxTba0yY/a6VVh9tSqoP1cVawvm/H4Qoy4Bo4hflm/l7S0hlKCl/KKkFudXvOasJG6rwI8VMOCjPtz+hZ47A3dpx8v2fkwurOrx/4niCAaaV7fqGDlpLxt+ZH0TEiRJx7EiBwMeiKKiq5EB/jzIzlZ6b0uP53iL//9LxbiRhWFJb7qTPvTbslfAXMOeBnFYl/5vBMuMeDYX4C0qIKw7qhw6jGAMhCXDi2K3d9NAt25BzVGEddg8jsQt2NQ//JLKY9hRrJUNDgxoSl6Q+7H9hZArXDQzXK0Oun3TLdRpnW3k7BMkOuprPJjLBMY49VtVHRZexD1GoLDdgKiB91g7J+LK9BVOwwehSIKouJI5SmzdjeWj506rWimM568R9kJiDy4gjyVd6/svOjUnXJyiMzcb/4glprrmec9zjN0ucVsj4VKD5IXhNba4nzt3Eu00872SnfIjMH4WD/p5i/bLnyXOVZtp/5KsswTzp18+2Wa4i7oc6FDAp2MBKmdj98NxaX/snUYqxeDG7jEESuCl4xPby+BDyEp2sRJ5dLuPytegJD/ximIP/7W9JTH5ZnItJU8G/aA1xdetPgxdBq1jCntqERnkIRisDAR2QiYGb24vbuuwgg3nECu4xnLjIDWce+OUCWZ3lk91L5/X4LuIwgaZK4TJ6NlhPgUm7cm2y58lzlWbaf+SrLME86dfPtlmuIu6HOhQwKdjASpnY/fDcWl/7J1GKsXgxu4xBErgpeMT28vgQ8hKdrESeXS7j8rXoCQ/8YpiD/+1vSUx+W5lv092zI1w1FjuvazPCXZDr5aLAp6JQSvposcCNhjJxtWQ3dzlBtgQGCPM4hH3WsYt4/xlkuAdF80MfNXWWcksMht8QEd2ROA73sP+t7rP9M5iqp9ie8gzr7wTkPvrpAbJWyqp/oR/DmKKS8mcFYmezonOlwt/QP8kKmjX9KKmfInAE1GR/MhzkiRegS2qu9/U9E9a8FNnwVLuyRAzalzIAS8Tp/NYcmNNE/LhwCscc4ONYo1YwcB/t/+HZr1yPjixkdcscRJAsa2SXNzHODs/+29cYRpsCTQW6d43kZUBYyxM7p84Cl1fxEmBhYrW/+XhFj3Kgbkpiv8hdeLL+Qc/TjTHgO+IWi36PbzqZa91A=",
        encryptedXml);
  }

  @Test
  @Tag("IntegrationTest")
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
  @Tag("IntegrationTest")
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
                .courtType("courtType")
                .courtPlace("courtPlace")
                .date("date")
                .fileNumber("fileNumber")
                .build());
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .creationtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .coreData(coreData)
            .previousDecisions(previousDecisions)
            .texts(texts)
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
