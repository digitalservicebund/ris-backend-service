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
            .court(new Court("courtType", null, null))
            .category("category")
            .ecli("ecli")
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
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
            .id(99L)
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .coreData(coreData)
            .previousDecisions(previousDecisions)
            .texts(texts)
            .build();

    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals(
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDsQUfTAw9rNE1esKkprKcKLZtd7IHfw4GJydedmPZLTdvj8gtBupPi+gOTh5k2GBNPDFWvJwQdNr4HGDgrNGnw7S/LQVrpamcxw6Ld4+KeqvUMznaj7uQmQMvpBIWdiGsDL+VUAR2W0HKvtrB3d48dOgcXcVHluk8Cc+1kozOELjVzYA3aZRoI8fJ3edIqtBCDvdiv38kBOQMwYObr8S+BGhnQfAPB/unY9zMDw9iGVd18XHl2WmfYs2hmqzcn8riZeSLYcXd1ceql9AlpXc+Ya1CrRLVIaPd+ru9K78Rl5EG3wqkHUY+iRrY18iQcpwAAjyB38ds4T+D0uG86XzmEelUPrYBzigqkdGIYzAp4n5cV7dz8vt+2u5HFUI+qWb81+IKDHUpbeln+gaxZ24PovZUruxkiJUWNTM82yYoKdAZHvV/DO6Vny5IF/2aT/2jdcFS0gSMzDvN8Gz1+VMomHt+Xluw+xQJniMGR+bfhm3ma+fcn5dAFjm4GSaZL6HlwZSUbUPJiDjbZfgFtIx8Vs8N9gYq6e7hwMi5X7VFawuqFV9fXyVx5bDwxz5njT/RlstdIKiZ7GBBjE38OMWJ7ufmyMuTCzj/cB1IJdWJ1IRuXn3JoTsoU99PxE55uz/TUSWXE121RvVNcQZvEDb++oKU05Oks3fnfdgf88mM1nHBf8BD7rEfRvWasmBIDNEz/Kv5OpWZwLde2TyUeZjQfloxdf9PbPTlTb8Jm4BPhPJkFNXi4q80CHJUZsbKRFY1tO2HgDkGHY55nPGqyAvw/ggxYy0oyIVmGa+dzdzj+CHaJIO7intDUDbKFrQ7GZuVWDfr7O4x2wtOOjLDMaBm6g+g8hD6qsx881GDS4rYIt4U1JtpS85i5xCsgDSBJbID/c8jbeRiPJy2fL6271A1mvgI2MksBW+pSddycjoE/JNsJma0G2I7viADuHOj5kJUOiSDu4p7Q1A2yha0OxmblVur37iaR6Ra2YOrx36qyPhJSAN7bCJtrqHv7Zy90iQuwnv3xD1/hxXBUoXX7TANv+6h26CDbSMUG3NF80BlAkcnfC0y+8kqWpwh1wx1lR/K8SxCkTc8/buNH+vF/XrKkKKDFwlaa7gNpEfjossU7H24ZiWftPlMkR/eduMIEGFf3CZmtBtiO74gA7hzo+ZCVDsA102I+8AoiHgMdiHO0wcC6sXAOhwOcYuNoMuhn+FHLWng5h5oqbEJtHwUkFDUjt",
        encryptedXml);
  }

  @Test
  @Tag("IntegrationTest")
  void test_invalidDocumentUnitMissingMandatoryFields_shouldReturnEmptyString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .id(99L)
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
            .category("category")
            .procedure("procedure")
            .ecli("ecli")
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
                .id(99L)
                .courtType("courtType")
                .courtPlace("courtPlace")
                .date("date")
                .fileNumber("fileNumber")
                .documentnumber(documentNr)
                .build());
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .id(99L)
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
