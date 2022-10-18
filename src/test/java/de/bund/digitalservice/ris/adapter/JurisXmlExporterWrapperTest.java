package de.bund.digitalservice.ris.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.bund.digitalservice.ris.domain.CoreData;
import de.bund.digitalservice.ris.domain.DocumentUnit;
import de.bund.digitalservice.ris.domain.PreviousDecision;
import de.bund.digitalservice.ris.domain.Texts;
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
  private final String documentNr = "ABCDE2022000001";

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
            .fileNumber("fileNumber")
            .courtType("courtType")
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
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDvKIaJibbD8SPkVIHgo1Pm0P//idUMCKXkp66tix1uuktvT8MDDNuw/lxSC+QT4xLjb03l66TQJxPwKlA30WiJwQ/gz53ieE7bPwHfqSWxfwC6Fvr69ik646jsSsJUMCSvZmTQsAXjxN/nnCJbBeDUmRpoRcv4ndk9JUpq8p3kpChhNAs/Njk8f6UBrg9XUxjPFsrV1O7chj2MmekyVWt5re/lNwjRFd5t7fWTpFirBK1aZUHuQOrHbanOR5ML7mIsjyB38ds4T+D0uG86XzmEelUPrYBzigqkdGIYzAp4n5cV7dz8vt+2u5HFUI+qWb81+IKDHUpbeln+gaxZ24PovZUruxkiJUWNTM82yYoKdAZHvV/DO6Vny5IF/2aT/2jdcFS0gSMzDvN8Gz1+VMomHt+Xluw+xQJniMGR+bfhm3ma+fcn5dAFjm4GSaZL6HlwZSUbUPJiDjbZfgFtIx8Vs8N9gYq6e7hwMi5X7VFawuqFV9fXyVx5bDwxz5njT/RlstdIKiZ7GBBjE38OMWJ7ufmyMuTCzj/cB1IJdWJ1IRuXn3JoTsoU99PxE55uz/TUSWXE121RvVNcQZvEDb++oKU05Oks3fnfdgf88mM1nHBf8BD7rEfRvWasmBIDNEz/Kv5OpWZwLde2TyUeZjQfloxdf9PbPTlTb8Jm4BPhPJkFNXi4q80CHJUZsbKRFY1tO2HgDkGHY55nPGqyAvw/ggxYy0oyIVmGa+dzdzj+CHaJIO7intDUDbKFrQ7GZuVWDfr7O4x2wtOOjLDMaBm6g+g8hD6qsx881GDS4rYIt4U1JtpS85i5xCsgDSBJbID/c8jbeRiPJy2fL6271A1mvgI2MksBW+pSddycjoE/JNsJma0G2I7viADuHOj5kJUOwDXTYj7wCiIeAx2Ic7TBwLqxcA6HA5xi42gy6Gf4UctaeDmHmipsQm0fBSQUNSO0=",
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
            .fileNumber("fileNumber")
            .courtType("courtType")
            .category("category")
            .procedure("procedure")
            .ecli("ecli")
            .appraisalBody("appraisalBody")
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
            .courtLocation("courtLocation")
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
