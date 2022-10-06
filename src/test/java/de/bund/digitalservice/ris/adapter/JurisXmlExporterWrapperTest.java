package de.bund.digitalservice.ris.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.domain.CoreData;
import de.bund.digitalservice.ris.domain.DocumentUnit;
import de.bund.digitalservice.ris.domain.PreviousDecision;
import de.bund.digitalservice.ris.domain.Texts;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class JurisXmlExporterWrapperTest {

  private JurisXmlExporter jurisXmlExporter;
  private String encryptedXml;
  private ObjectMapper objectMapper;
  private DocumentUnit documentUnit;
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String documentNr = "ABCDE2022000001";
  private final CoreData coreData =
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
  private final Texts texts =
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
  private List<PreviousDecision> previousDecisions =
      List.of(
          PreviousDecision.builder()
              .courtType("courtType")
              .courtPlace("courtPlace")
              .date("date")
              .fileNumber("fileNumber")
              .build());
  @Mock private Instant mockInstant;

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    objectMapper = new ObjectMapper();
    documentUnit =
        DocumentUnit.builder()
            .id(99L)
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .creationtimestamp(mockInstant)
            .fileuploadtimestamp(mockInstant)
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .coreData(coreData)
            .previousDecisions(previousDecisions)
            .texts(texts)
            .build();
  }

  @Test
  void testGetCorrectEncryptedXml() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);
    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals(
        "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",
        encryptedXml);
  }
}
