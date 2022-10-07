package de.bund.digitalservice.ris.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.bund.digitalservice.ris.domain.CoreData;
import de.bund.digitalservice.ris.domain.DocumentUnit;
import de.bund.digitalservice.ris.domain.PreviousDecision;
import de.bund.digitalservice.ris.domain.Texts;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
  }

  @Test
  void test_validDocumentUnit_shouldReturnEncryptedXMLString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    CoreData coreData =
        CoreData.builder()
            .fileNumber("fileNumber")
            .courtType("courtType")
            .category("category")
            .ecli("ecli")
            .decisionDate("decisionDate")
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
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDvKIaJibbD8SPkVIHgo1Pm0P//idUMCKXkp66tix1uuktvT8MDDNuw/lxSC+QT4xLjb03l66TQJxPwKlA30WiJwQ/gz53ieE7bPwHfqSWxfwC6Fvr69ik646jsSsJUMCSvZmTQsAXjxN/nnCJbBeDUmVmkVS2e9HEdz+IEFBc/oLpal7N7UjAVhbAuj0W2XHVTJWsjLHN9oKELe14AO64Sw3RAz2JEKNKc3rvoyTb/kqhbJdWjdg8uFJG+YvJjQ3lf2BI7qpGRWDO1pDtOc3oddne4yMlZbJXXcR6CCTuUR1VoV2vULTk09FpW0FCxzPtB2Gj0v7lcc5rbn/t2A9I+emFGuWeBoYR0ELqFtLqJHbUnUjVpq01TLkSO+L8x7+xA9vdkNUe+8ATp/g3Qe0hHsQNuWp6C/SRMlMkGW4E5AKgAuDxfhPNu5EeZZN8F7VJ11kfY5L+kQDzBazGHBsx/LaRXSYNU3Nn2J1VW/EApfbWQcwcVgD2EnO08IXD9jf41xVvlSH6bpQtGZ4/95rrfdnOt5ZC2peAmCRbOP0rXYszr5aLAp6JQSvposcCNhjJwAKEBD4UdrXqG4jXgt9oo7IibCyeP9n9u9P9figKMKeeG9l3sa1AZsRQD0L3PNwX+Gi8F09mDnBXJxEO+rhDV0c5JGd0tpwPzI0jZZTbL9DK1EZUdJs842KvswLkcXPcWMHinWLJNms2NhbrBnHobTbJWyqp/oR/DmKKS8mcFYmezonOlwt/QP8kKmjX9KKmcGEaPeUrTky8p/PbcZ+o1ViZKoD0waTRytBWcGeg4t9NYwp7ahEZ5CEYrAwEdkImDS7zOORRKDiDOv16+Gj1QzQEW7U9QJZqB/se+UDvgF7f5zyc+IzRkWk3u2rtbGRSRlTRoNl1XfVMsL57eX4swmO6TAzQ7KmSsnPcjXU+SiMUFb8lwYAeb12mWlZVWDfTU=",
        encryptedXml);
  }

  @Test
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

  // In order to be in sync with the model, all possible fields are set in this test
  // documentUnit and checked for null fields. Everytime a new field is added to the
  // data model this test will fail, as it is not set in the test documentUnit. The
  // test documentUnit needs to be updated accordingly to be in sync with the model.
  @Test
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
            .decisionDate("decisionDate")
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
  }
}
