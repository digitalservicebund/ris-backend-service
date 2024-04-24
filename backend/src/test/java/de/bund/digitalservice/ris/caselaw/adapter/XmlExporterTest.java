package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.config.ConverterConfig;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporterException;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({ConverterConfig.class})
@Slf4j
class XmlExporterTest {
  @Autowired private ObjectMapper objectMapper;

  @Test
  void testExporter()
      throws XmlExporterException, ParserConfigurationException, TransformerException {
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .coreData(generateCoreData())
            .documentNumber("document number")
            .previousDecisions(generatePreviousDecisions())
            .ensuingDecisions(generateEnsuingDecisions())
            .texts(generateTexts())
            .status(generateStatus())
            .contentRelatedIndexing(generateContentRelatedIndexing())
            .build();

    String encryptedXml =
        new JurisXmlExporterWrapper(objectMapper).generateEncryptedXMLString(documentUnit);

    assertThat(encryptedXml)
        .isEqualTo(
            "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");
  }

  private ContentRelatedIndexing generateContentRelatedIndexing() {
    return ContentRelatedIndexing.builder()
        .activeCitations(generateActiveCitations())
        .norms(generateNorms())
        .keywords(generateKeywords())
        .fieldsOfLaw(generateFieldOfLaws())
        .build();
  }

  private List<FieldOfLaw> generateFieldOfLaws() {
    List<Norm> norms =
        List.of(
            Norm.builder()
                .abbreviation("field of law norm abbreviation 1")
                .singleNormDescription("field of law norm description 1")
                .build(),
            Norm.builder()
                .abbreviation("field of law norm abbreviation 2")
                .singleNormDescription("field of law norm description 2")
                .build());
    return List.of(
        FieldOfLaw.builder()
            .identifier("norm identifier 1")
            .text("norm text 1")
            .norms(norms)
            .build(),
        FieldOfLaw.builder().identifier("norm identifier 2").text("norm text 2").build());
  }

  private List<String> generateKeywords() {
    return List.of("keyword 1", "keyword 2");
  }

  private List<NormReference> generateNorms() {
    List<SingleNorm> singleNorms1 =
        List.of(
            SingleNorm.builder()
                .singleNorm("single norm 1")
                .dateOfVersion(LocalDate.parse("1999-01-17"))
                .dateOfRelevance("1999")
                .build(),
            SingleNorm.builder().singleNorm("single norm 2").build(),
            SingleNorm.builder().dateOfVersion(LocalDate.parse("1985-07-26")).build(),
            SingleNorm.builder().dateOfRelevance("1973").build());
    return List.of(
        NormReference.builder()
            .singleNorms(singleNorms1)
            .normAbbreviation(
                NormAbbreviation.builder().abbreviation("norm abbreviation 1").build())
            .build(),
        NormReference.builder()
            .singleNorms(Collections.emptyList())
            .normAbbreviationRawValue("norm abbreviation raw value")
            .build());
  }

  private List<ActiveCitation> generateActiveCitations() {
    return List.of(
        ActiveCitation.builder()
            .citationType(
                CitationType.builder().jurisShortcut("ct1").label("citation type 1").build())
            .fileNumber("active citation file number 1")
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("acdt1")
                    .label("active citation document type 1")
                    .build())
            .court(
                Court.builder()
                    .type("active citation court type 1")
                    .location("active citation court location 1")
                    .build())
            .decisionDate(LocalDate.parse("2001-07-22"))
            .documentNumber("active citation document number")
            .build(),
        ActiveCitation.builder()
            .citationType(
                CitationType.builder().jurisShortcut("ct2").label("citation type2").build())
            .fileNumber("active citation file number 2")
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("acdt2")
                    .label("active citation document type 2")
                    .build())
            .court(
                Court.builder()
                    .type("active citation court type 2")
                    .location("active citation court location 2")
                    .build())
            .decisionDate(LocalDate.parse("2005-11-29"))
            .build());
  }

  private Status generateStatus() {
    LocalDateTime localDateTime = LocalDateTime.of(2020, Month.MAY, 6, 17, 35);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"));

    return Status.builder()
        .publicationStatus(PublicationStatus.PUBLISHED)
        .withError(false)
        .createdAt(zonedDateTime.toInstant())
        .build();
  }

  private Texts generateTexts() {
    return Texts.builder()
        .decisionReasons("decision reasons")
        .caseFacts("case facts")
        .reasons("reasons")
        .tenor("tenor")
        .headnote("headnote")
        .guidingPrinciple("guiding principle")
        .headline("headline")
        .decisionName("decision name")
        .build();
  }

  private List<EnsuingDecision> generateEnsuingDecisions() {
    return List.of(
        EnsuingDecision.builder()
            .fileNumber("pending decision file number")
            .court(
                Court.builder()
                    .type("pending decision court type")
                    .location("pending decision court location")
                    .build())
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("peddt")
                    .label("pending decision document type")
                    .build())
            .note("pending decision note")
            .pending(true)
            .build(),
        EnsuingDecision.builder()
            .fileNumber("ensuing decision file number")
            .court(
                Court.builder()
                    .type("ensuing decision court type")
                    .location("ensuing decision court location")
                    .build())
            .decisionDate(LocalDate.parse("2005-06-17"))
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("eddt")
                    .label("ensuing decision document type")
                    .build())
            .note("ensuing decision note")
            .pending(false)
            .documentNumber("ensuing decision document number")
            .build());
  }

  private List<PreviousDecision> generatePreviousDecisions() {
    return List.of(
        PreviousDecision.builder()
            .fileNumber("previous decision file number 1")
            .deviatingFileNumber("previous decision deviating file number 1")
            .court(
                Court.builder()
                    .type("previous decision court type 1")
                    .location("previous decision court location 1")
                    .build())
            .decisionDate(LocalDate.parse("2005-06-17"))
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("pddt1")
                    .label("previous decision document type 1")
                    .build())
            .documentNumber("previous decision document number 1")
            .build(),
        PreviousDecision.builder()
            .fileNumber("previous decision file number 2")
            .court(
                Court.builder()
                    .type("previous decision court type 2")
                    .location("previous decision court location 2")
                    .build())
            .decisionDate(LocalDate.parse("2013-08-12"))
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("pddt2")
                    .label("previous decision document type 2")
                    .build())
            .build());
  }

  private CoreData generateCoreData() {
    return CoreData.builder()
        .court(Court.builder().type("court type").location("court location").build())
        .leadingDecisionNormReferences(
            List.of("leading decision norm reference 1", "leading decision norm reference 2"))
        .documentationOffice(
            DocumentationOffice.builder().abbreviation("documentation office").build())
        .inputTypes(List.of("input type 1", "input type 2"))
        .deviatingDecisionDates(
            List.of(LocalDate.parse("2011-01-09"), LocalDate.parse("2011-01-07")))
        .deviatingEclis(List.of("deviating ecli 1", "deviating ecli 2"))
        .previousProcedures(List.of("procedure 1", "procedure 2"))
        .deviatingCourts(List.of("deviating court 1", "deviating court 2"))
        .deviatingFileNumbers(List.of("deviating file number 1", "deviating file number 2"))
        .fileNumbers(List.of("file number 1", "file number 2"))
        .region("region")
        .legalEffect("ja")
        .documentType(DocumentType.builder().jurisShortcut("dt").label("document type").build())
        .appraisalBody("appraisal body")
        .ecli("ecli")
        .decisionDate(LocalDate.parse("2011-01-08"))
        .procedure(Procedure.builder().label("procedure 3").build())
        .build();
  }
}
