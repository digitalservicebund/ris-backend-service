package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendingProceedingFullLdmlTransformerTest {

  private static PendingProceeding testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static PendingProceedingFullLdmlTransformer transformer;
  private static UUID documentationUnitId;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    transformer = new PendingProceedingFullLdmlTransformer(documentBuilderFactory);

    documentationUnitId = UUID.randomUUID();

    PreviousDecision related1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(Court.builder().type("Test court type").build())
            .documentType(DocumentType.builder().label("Test decision type").build())
            .fileNumber("Test file number")
            .documentNumber("Test document number 1")
            .build();
    PreviousDecision related2 =
        related1.toBuilder().documentNumber("Test document number 2").build();

    testDocumentUnit =
        PendingProceeding.builder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .type("Gerichtstyp")
                            .location("Gerichtsort")
                            .label("Gerichtstyp Gerichtsort")
                            .build())
                    .documentType(DocumentType.builder().label("Anhängiges Verfahren").build())
                    .fileNumbers(List.of("Aktenzeichen"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .documentationOffice(DocumentationOffice.builder().abbreviation("BFH").build())
                    .isResolved(true)
                    .resolutionDate(LocalDate.of(2021, 10, 15))
                    .build())
            .documentNumber("Dokumentnummer")
            .shortTexts(
                PendingProceedingShortTexts.builder()
                    .legalIssue("Rechtsfrage")
                    .appellant("Rechtsmittelführer")
                    .admissionOfAppeal("Rechtsmittelzulassung")
                    .resolutionNote("Erledigungsmitteilung")
                    .build())
            .previousDecisions(List.of(related1, related2))
            .build();
  }

  @Test
  @DisplayName("Fallback title test")
  void documentNumberIsFallbackTitleTest() {
    String expected =
        """
      <akn:header>
         <akn:p>Gerichtstyp Gerichtsort, 01.01.2020, Aktenzeichen</akn:p>
      </akn:header>
     """;
    CaseLawLdml ldml = transformer.transformToLdml(testDocumentUnit);
    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Fulfilled pending proceeding")
  void shouldBeFulfilled() {
    String expected =
        """
      <akn:decision>
        <akn:blockname="Erledigungsmitteilung">
          <akn:embeddedStructure>
            <akn:palternativeTo="textWrapper">Erledigungsmitteilung</akn:p>
          </akn:embeddedStructure>
        </akn:block>
      </akn:decision>
     """;
    CaseLawLdml ldml = transformer.transformToLdml(testDocumentUnit);
    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Mixed text in header")
  void testTransform_mixedTextInHeader() {
    String expected =
        """
                                <akn:header>
                                  <akn:p alternativeTo="textWrapper">Hello</akn:p>
                                  <akn:p> paragraph</akn:p>
                                  <akn:p alternativeTo="textWrapper"> world!</akn:p>
                                </akn:header>
                                """;
    PendingProceeding otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(
                PendingProceedingShortTexts.builder()
                    .headline("Hello<p> paragraph</p> world!")
                    .legalIssue("legal issue")
                    .build())
            .build();

    CaseLawLdml ldml = transformer.transformToLdml(otherLongTextCaseLaw);

    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Keywords")
  void testTransform_keywords() {
    String expected =
        """
                                <akn:keyworddictionary="attributsemantik-noch-undefiniert"showAs="attributsemantik-noch-undefiniert"value="keyword1"/>
                                """;
    PendingProceeding otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().keywords(List.of("keyword 1")).build())
            .build();

    CaseLawLdml ldml = transformer.transformToLdml(otherLongTextCaseLaw);

    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Entire LDML of unresolved pending proceeding")
  void testEntireLdml() {
    var documentationUnit = getEntireDocumentationUnit();
    var expected =
        String.format(
            """
<?xml version="1.0" encoding="utf-8"?>
<akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0/WD17"
                xmlns:ris="http://example.com/0.1/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://docs.oasis-open.org/legaldocml/ns/akn/3.0/WD17 https://docs.oasis-open.org/legaldocml/akn-core/v1.0/csprd02/part2-specs/schemas/akomantoso30.xsd">
   <akn:judgment name="attributsemantik-noch-undefiniert">
      <akn:meta>
         <akn:identification source="attributsemantik-noch-undefiniert">
            <akn:FRBRWork>
               <akn:FRBRthis value="YYTestDoc0013"/>
               <akn:FRBRuri value="YYTestDoc0013"/>
               <akn:FRBRalias name="uebergreifende-id" value="%s"/>
               <akn:FRBRdate date="2020-01-01" name="Mitteilungsdatum"/>
               <akn:FRBRauthor href="attributsemantik-noch-undefiniert"/>
               <akn:FRBRcountry value="de"/>
            </akn:FRBRWork>
            <akn:FRBRExpression>
               <akn:FRBRthis value="YYTestDoc0013/dokument"/>
               <akn:FRBRuri value="YYTestDoc0013/dokument"/>
               <akn:FRBRdate date="2020-01-01" name="Mitteilungsdatum"/>
               <akn:FRBRauthor href="attributsemantik-noch-undefiniert"/>
               <akn:FRBRlanguage language="de"/>
            </akn:FRBRExpression>
            <akn:FRBRManifestation>
               <akn:FRBRthis value="YYTestDoc0013/dokument.xml"/>
               <akn:FRBRuri value="YYTestDoc0013/dokument.xml"/>
               <akn:FRBRdate date="2020-01-01" name="Mitteilungsdatum"/>
               <akn:FRBRauthor href="attributsemantik-noch-undefiniert"/>
            </akn:FRBRManifestation>
         </akn:identification>
         <akn:classification source="attributsemantik-noch-undefiniert">
            <akn:keyword dictionary="attributsemantik-noch-undefiniert"
                         showAs="attributsemantik-noch-undefiniert"
                         value="keyword test"/>
         </akn:classification>
         <akn:proprietary source="attributsemantik-noch-undefiniert">
            <ris:meta>
               <ris:previousDecisions>
                  <ris:previousDecision date="2020-01-01">
                     <ris:documentNumber>previous decision document number 1</ris:documentNumber>
                     <ris:fileNumber>previous decision file number</ris:fileNumber>
                     <ris:courtType>previous decision court type</ris:courtType>
                  </ris:previousDecision>
                  <ris:previousDecision date="2020-01-01">
                     <ris:documentNumber>previous decision document number 2</ris:documentNumber>
                     <ris:fileNumber>previous decision file number</ris:fileNumber>
                     <ris:courtType>previous decision court type</ris:courtType>
                  </ris:previousDecision>
               </ris:previousDecisions>
               <ris:fileNumbers>
                  <ris:fileNumber>Aktenzeichen</ris:fileNumber>
               </ris:fileNumbers>
               <ris:documentType>Anhängiges Verfahren</ris:documentType>
               <ris:courtLocation>Gerichtsort</ris:courtLocation>
               <ris:courtType>Gerichtstyp</ris:courtType>
               <ris:legalForces>
                  <ris:legalForce>legalForce test</ris:legalForce>
               </ris:legalForces>
               <ris:fieldOfLaws>
                  <ris:fieldOfLaw>Sachgebiet</ris:fieldOfLaw>
               </ris:fieldOfLaws>
               <ris:deviatingCourts>
                  <ris:deviatingCourt>Abweichendes Gericht</ris:deviatingCourt>
               </ris:deviatingCourts>
               <ris:deviatingDates>
                  <ris:deviatingDate>2010-05-12</ris:deviatingDate>
               </ris:deviatingDates>
               <ris:deviatingDocumentNumbers>
                  <ris:deviatingDocumentNumber>Abweichende Dokumentnummer</ris:deviatingDocumentNumber>
               </ris:deviatingDocumentNumbers>
               <ris:deviatingFileNumbers>
                  <ris:deviatingFileNumber>Abweichendes Aktenzeichen</ris:deviatingFileNumber>
               </ris:deviatingFileNumbers>
               <ris:publicationStatus>PUBLISHED</ris:publicationStatus>
               <ris:error>false</ris:error>
               <ris:documentationOffice>BFH</ris:documentationOffice>
            </ris:meta>
         </akn:proprietary>
      </akn:meta>
      <akn:header>
         <akn:p alternativeTo="textWrapper">Titelzeile</akn:p>
      </akn:header>
      <akn:judgmentBody>
         <akn:motivation>
            <akn:p alternativeTo="textWrapper">Rechtsfrage</akn:p>
         </akn:motivation>
         <akn:introduction>
            <akn:block name="Rechtsmittelführer">
               <akn:embeddedStructure>
                  <akn:p alternativeTo="textWrapper">Rechtsmittelführer</akn:p>
               </akn:embeddedStructure>
            </akn:block>
            <akn:block name="Rechtsmittelzulassung">
               <akn:embeddedStructure>
                  <akn:p alternativeTo="textWrapper">Rechtsmittelzulassung</akn:p>
               </akn:embeddedStructure>
            </akn:block>
         </akn:introduction>
      </akn:judgmentBody>
   </akn:judgment>
</akn:akomaNtoso>
           """,
            documentationUnitId);

    // Act
    CaseLawLdml ldml = transformer.transformToLdml(documentationUnit);

    // Assert
    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent().contains(expected);
  }

  PendingProceeding getEntireDocumentationUnit() {
    PreviousDecision previousDecision1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(Court.builder().type("previous decision court type").build())
            .documentType(DocumentType.builder().label("previous decision document type").build())
            .fileNumber("previous decision file number")
            .documentNumber("previous decision document number 1")
            .build();
    PreviousDecision previousDecision2 =
        previousDecision1.toBuilder().documentNumber("previous decision document number 2").build();

    return PendingProceeding.builder()
        .uuid(documentationUnitId)
        .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .coreData(
            CoreData.builder()
                .court(
                    Court.builder()
                        .type("Gerichtstyp")
                        .location("Gerichtsort")
                        .label("Gerichtstyp Gerichtsort")
                        .build())
                .documentType(DocumentType.builder().label("Anhängiges Verfahren").build())
                .fileNumbers(List.of("Aktenzeichen"))
                .decisionDate(LocalDate.of(2020, 1, 1))
                .deviatingCourts(List.of("Abweichendes Gericht"))
                .deviatingFileNumbers(List.of("Abweichendes Aktenzeichen"))
                .deviatingDecisionDates(List.of(LocalDate.of(2010, 5, 12)))
                .deviatingDocumentNumbers(List.of("Abweichende Dokumentnummer"))
                .documentationOffice(DocumentationOffice.builder().abbreviation("BFH").build())
                .isResolved(false)
                .resolutionDate(null)
                .build())
        .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .documentNumber("YYTestDoc0013")
        .shortTexts(
            PendingProceedingShortTexts.builder()
                .headline("Titelzeile")
                .legalIssue("Rechtsfrage")
                .appellant("Rechtsmittelführer")
                .admissionOfAppeal("Rechtsmittelzulassung")
                .resolutionNote(null)
                .build())
        .contentRelatedIndexing(
            ContentRelatedIndexing.builder()
                .activeCitations(
                    List.of(
                        ActiveCitation.builder()
                            .citationType(CitationType.builder().label("citation test").build())
                            .build()))
                .keywords(List.of("keyword test"))
                .fieldsOfLaw(List.of(FieldOfLaw.builder().text("Sachgebiet").build()))
                .norms(
                    List.of(
                        NormReference.builder()
                            .singleNorms(
                                List.of(
                                    SingleNorm.builder()
                                        .singleNorm("singleNorm test")
                                        .legalForce(
                                            LegalForce.builder()
                                                .type(
                                                    LegalForceType.builder()
                                                        .label("legalForce test")
                                                        .build())
                                                .build())
                                        .build()))
                            .normAbbreviation(
                                NormAbbreviation.builder()
                                    .abbreviation("normReference test")
                                    .build())
                            .build()))
                .jobProfiles(List.of("jobProfile test"))
                .dismissalGrounds(List.of("dismissalGround test"))
                .dismissalTypes(List.of("dismissalType test"))
                .collectiveAgreements(List.of("collectiveAgreement test"))
                .hasLegislativeMandate(true)
                .build())
        .previousDecisions(List.of(previousDecision1, previousDecision2))
        .caselawReferences(
            List.of(
                Reference.builder()
                    .referenceType(ReferenceType.CASELAW)
                    .citation("citation")
                    .referenceSupplement("reference supplement")
                    .legalPeriodical(
                        LegalPeriodical.builder().abbreviation("LegalPeriodical").build())
                    .legalPeriodicalRawValue("LegalPeriodical")
                    .build()))
        .literatureReferences(
            List.of(
                Reference.builder()
                    .referenceType(ReferenceType.LITERATURE)
                    .author("author")
                    .citation("citation")
                    .documentType(DocumentType.builder().label("doc type").build())
                    .build()))
        .build();
  }
}
