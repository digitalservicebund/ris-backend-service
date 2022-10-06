package de.bund.digitalservice.ris.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.domain.DocumentUnit;
import de.bund.digitalservice.ris.domain.DocumentUnitBuilder;
import de.bund.digitalservice.ris.domain.DocumentUnitDTO;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JurisXmlExporterWrapperTest {

  private JurisXmlExporter jurisXmlExporter;
  private String encryptedXml;
  private ObjectMapper objectMapper;
  private DocumentUnit documentUnit;
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String documentNr = "ABCDE2022000001";

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    objectMapper = new ObjectMapper();
    DocumentUnitDTO documentUnitDTO = new DocumentUnitDTO();
    documentUnitDTO.setFilename("doc.docx");
    documentUnitDTO.setFileNumber("fileNumber123");
    documentUnitDTO.setReasons("reasons123");
    documentUnitDTO.setCourtLocation("courtLocation123");
    documentUnitDTO.setCourtType("courtType123");
    documentUnitDTO.setCategory("category123");
    documentUnitDTO.setProcedure("procedure123");
    documentUnitDTO.setId(123L);
    documentUnitDTO.setUuid(TEST_UUID);
    documentUnit = DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build();
  }

  @Test
  void testGetCorrectEncryptedXml() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);
    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals(
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpOKeB4oZ4WktKvWrtHCAapWcjgx92YWtBRc24V8MW3z7q1VCKmc8ugMQfys7wt5ihnHkvXkcdWHoCrR0pFmXMmp/TUaldemOb21axpPFJvGGR/a4lafI2ryzJQu8XDWc3bOlyEXdPEE3nETr2WFjuVU3zSBSl/fpzJa/Xx61lW6Rl0NfaoTdrjyuLbBFSJkGF5efcmhOyhT30/ETnm7P9NdQq0S1SGj3fq7vSu/EZeRDmU14NrQg1jNpjr+xJOKqc1IIHL+PNBe6+pruVy4n8aNbNQygEbzHXJ69QrcQ1zcaST3GXp+kCv/YhEW8nJpyKLCsF4YaI3HwWKAOCTZQcR8NcAecCRCPDZ1drD2711ooBzh0MF/2WT8hhfisMpp7LHWTGZp7zLhECuD2RmGEm3ZBB3xGBhKFBE0gNttWYFjL2oZ5BWzrAhPOsstNgzRly5Cvwg/qNd08owtpX5EuX9M4LzXULjFXu3EE7g1qjsJVMcVodrhMH7AE+IYmI6YfB211jqLlU2kKI77Llx2lDHb6dBLfzAYXMdyv2CH+nQuhBW/JcGAHm9dplpWVVg301",
        encryptedXml);
  }
}
