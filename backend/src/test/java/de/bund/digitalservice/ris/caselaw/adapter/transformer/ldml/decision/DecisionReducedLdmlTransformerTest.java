package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.Notation;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.TestUtils;
import de.bund.digitalservice.ris.caselaw.domain.AbuseFee;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.Addressee;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmission;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmitter;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreement;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Correction;
import de.bund.digitalservice.ris.caselaw.domain.CorrectionType;
import de.bund.digitalservice.ris.caselaw.domain.CountryOfOrigin;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCode;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.Definition;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.domain.IncomeType;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.ObjectValue;
import de.bund.digitalservice.ris.caselaw.domain.OriginOfTranslation;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingType;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedPendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.TranslationType;
import de.bund.digitalservice.ris.caselaw.domain.TypeOfIncome;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appeal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealWithdrawal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import de.bund.digitalservice.ris.caselaw.domain.appeal.PkhPlaintiff;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtBranchLocation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.ParticipatingJudge;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

@ExtendWith(MockitoExtension.class)
class DecisionReducedLdmlTransformerTest {

  private static Decision testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static DecisionReducedLdmlTransformer subject;
  private static UUID documentationUnitId;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    subject = new DecisionReducedLdmlTransformer(documentBuilderFactory);
    documentationUnitId = UUID.randomUUID();
    createTestDocumentationUnit();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCasesForIncludedAttributes")
  void transformIncludedAttributes(String testName, String expected) {
    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCasesForExcludedAttributes")
  void transformExcludedAttributes(String testName, String expected) {
    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertFalse(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  void testEntireLdml() throws Exception {
    // Arrange
    Path expectedFilePath = Paths.get("src/test/resources/testdata/decision_reduced_ldml.xml");
    String expected = Files.readString(expectedFilePath, StandardCharsets.UTF_8);

    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());

    Diff diff =
        DiffBuilder.compare(expected)
            .withTest(fileContent.get())
            .withDifferenceEvaluator(TestUtils.ignoreAttributeEvaluator)
            .ignoreWhitespace()
            .checkForIdentical()
            .build();

    if (diff.hasDifferences()) {
      StringBuilder differences = new StringBuilder();
      diff.getDifferences().forEach(d -> differences.append(d.toString()).append("\n"));
      Assertions.fail("XMLs differ:\n" + differences);
    }
  }

  private static void createTestDocumentationUnit() {
    PreviousDecision previousDecision1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(
                Court.builder()
                    .type("previous decision court type")
                    .location("previous decision court location")
                    .build())
            .documentType(DocumentType.builder().label("previous decision document type").build())
            .fileNumber("previous decision file number")
            .documentNumber("previous decision document number 1")
            .build();
    PreviousDecision previousDecision2 =
        previousDecision1.toBuilder().documentNumber("previous decision document number 2").build();

    EnsuingDecision ensuingDecision1 =
        EnsuingDecision.builder()
            .decisionDate(LocalDate.of(2022, 10, 1))
            .court(Court.builder().type("ensuing decision court type").build())
            .documentType(DocumentType.builder().label("ensuing decision document type").build())
            .fileNumber("ensuing decision file number")
            .documentNumber("ensuing decision document number 1")
            .pending(true)
            .build();
    EnsuingDecision ensuingDecision2 =
        ensuingDecision1.toBuilder()
            .documentNumber("ensuing decision document number 2")
            .pending(true)
            .note("ensuing decision note")
            .build();

    testDocumentUnit =
        // Use empty DTO as basis as it creates empty lists for everything.
        DecisionTransformer.transformToDomain(new DecisionDTO()).toBuilder()
            .uuid(documentationUnitId)
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .note("note test")
            .coreData(
                CoreData.builder()
                    .ecli("ecli test")
                    .celexNumber("celex test")
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("documentationOffice").build())
                    .creatingDocOffice(
                        DocumentationOffice.builder()
                            .abbreviation("creatingDocumentationOffice")
                            .build())
                    .court(
                        Court.builder()
                            .isSuperiorCourt(false)
                            .isForeignCourt(false)
                            .label("courtLabel test")
                            .type("courtType")
                            .jurisdictionType("jurisdictionType")
                            .location("courtLocation")
                            .regions(List.of("NW"))
                            .build())
                    .courtBranchLocation(
                        CourtBranchLocation.builder().value("court branch location").build())
                    .sources(
                        List.of(
                            Source.builder()
                                .sourceRawValue("sourceRawValue test")
                                .value(SourceValue.S)
                                .build()))
                    .documentType(DocumentType.builder().label("documentType test").build())
                    .legalEffect("ja")
                    .fileNumbers(List.of("fileNumber test"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .appraisalBody("appraisalBody test")
                    .inputTypes(List.of("inputType test"))
                    .procedure(Procedure.builder().label("procedure test").build())
                    .previousProcedures(List.of("previous procedure test"))
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("documentationOffice").build())
                    .creatingDocOffice(
                        DocumentationOffice.builder()
                            .abbreviation("creatingDocumentationOffice")
                            .build())
                    .previousProcedures(List.of("previous procedure test"))
                    .procedure(Procedure.builder().label("procedure test").build())
                    .inputTypes(List.of("inputType test"))
                    .deviatingEclis(List.of("deviating ecli test"))
                    .deviatingCourts(List.of("deviating court"))
                    .deviatingFileNumbers(List.of("deviating fileNumber"))
                    .deviatingDecisionDates(List.of(LocalDate.of(2010, 5, 12)))
                    .yearsOfDispute(List.of(Year.now()))
                    .leadingDecisionNormReferences(List.of("leadingDecisionNormReference test"))
                    .build())
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .documentNumber("YYTestDoc0013")
            .longTexts(
                LongTexts.builder()
                    .caseFacts("<p>caseFacts test</p>")
                    .decisionReasons("<p>decisionGrounds test</p>")
                    .reasons("<p>grounds test</p>")
                    .dissentingOpinion("<p>dissenting test</p>")
                    .otherLongText("<p>otherLongText test</p>")
                    .tenor("<p>tenor test</p>")
                    .outline("<p>outline test</p>")
                    .participatingJudges(
                        List.of(
                            ParticipatingJudge.builder()
                                .name("Dr. Phil. Max Mustermann")
                                .referencedOpinions("referenced opinions test 1")
                                .build(),
                            ParticipatingJudge.builder()
                                .name("Richterin Maxima Mustermann")
                                .referencedOpinions("referenced opinions test 2")
                                .build()))
                    .corrections(
                        List.of(
                            Correction.builder()
                                .type(CorrectionType.BERICHTIGUNGSBESCHLUSS)
                                .build(),
                            Correction.builder()
                                .type(CorrectionType.SCHREIBFEHLERBERICHTIGUNG)
                                .description("Hauffen -> Haufen")
                                .date(LocalDate.of(2020, 1, 20))
                                .borderNumbers(List.of(1L, 3L))
                                .content("<p>Ersetzen von 'Hauffen' mit 'Haufen'</p>")
                                .build()))
                    .build())
            .shortTexts(
                ShortTexts.builder()
                    .guidingPrinciple("<p>guidingPrinciple test</p>")
                    .headline("<p>headline test</p>")
                    .decisionNames(List.of("decisionNames test"))
                    .headnote("<p>headNote test</p>")
                    .otherHeadnote("<p>otherHeadNote test</p>")
                    .build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .citationType(CitationType.builder().label("citation test").build())
                                .build()))
                    .keywords(List.of("keyword test"))
                    .fieldsOfLaw(
                        List.of(
                            FieldOfLaw.builder()
                                .identifier("AR-01-01-01")
                                .notation(Notation.NEW.toString())
                                .text("Verschulden bei Vertragsschluss (culpa in contrahendo)")
                                .build()))
                    .norms(
                        List.of(
                            NormReference.builder()
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("singleNorm test")
                                            .dateOfRelevance("2020")
                                            .dateOfVersion(LocalDate.of(2021, 2, 5))
                                            .legalForce(
                                                LegalForce.builder()
                                                    .region(
                                                        Region.builder()
                                                            .code("legalForce region code")
                                                            .longText("legalForce region longText")
                                                            .build())
                                                    .type(
                                                        LegalForceType.builder()
                                                            .label("legalForceType label")
                                                            .abbreviation(
                                                                "legalForceType abbreviation")
                                                            .build())
                                                    .build())
                                            .build(),
                                        SingleNorm.builder()
                                            .singleNorm("singleNorm 2 test")
                                            .dateOfRelevance("2022")
                                            .dateOfVersion(LocalDate.of(2022, 3, 6))
                                            .build()))
                                .normAbbreviation(
                                    NormAbbreviation.builder()
                                        .abbreviation("normReference test")
                                        .build())
                                .build(),
                            NormReference.builder()
                                .normAbbreviation(
                                    NormAbbreviation.builder()
                                        .abbreviation("normReference without SingleNorms")
                                        .decisionDate(
                                            LocalDate.of(2019, 4, 7)
                                                .atStartOfDay()
                                                .atZone(ZoneId.of("Europe/Berlin"))
                                                .toInstant())
                                        .documentId(123L)
                                        .documentNumber("KORE12345")
                                        .documentTypes(
                                            List.of(
                                                DocumentType.builder()
                                                    .label("documentType label")
                                                    .build()))
                                        .source("Source")
                                        .officialLongTitle("officialLongTitle")
                                        .officialShortTitle("officialShortTitle")
                                        .officialLetterAbbreviation("officialLetterAbbreviation")
                                        .region(
                                            Region.builder()
                                                .code("region code")
                                                .longText("region longtext")
                                                .build())
                                        .build())
                                .build()))
                    .jobProfiles(List.of("jobProfile test"))
                    .dismissalGrounds(List.of("dismissalGround test"))
                    .dismissalTypes(List.of("dismissalType test"))
                    .collectiveAgreements(
                        List.of(
                            CollectiveAgreement.builder().name("collectiveAgreement test").build()))
                    .hasLegislativeMandate(true)
                    .foreignLanguageVersions(
                        List.of(
                            ForeignLanguageVersion.builder()
                                .id(UUID.randomUUID())
                                .languageCode(
                                    LanguageCode.builder()
                                        .id(UUID.randomUUID())
                                        .label("Englisch")
                                        .isoCode("en")
                                        .isoCode3Letters("eng")
                                        .build())
                                .link("https://ihre-url-zur-englischen-übersetzung")
                                .build(),
                            ForeignLanguageVersion.builder()
                                .id(UUID.randomUUID())
                                .languageCode(
                                    LanguageCode.builder()
                                        .id(UUID.randomUUID())
                                        .label("Französisch")
                                        .isoCode("fr")
                                        .isoCode3Letters("fra")
                                        .build())
                                .link("https://ihre-url-zur-französischen-übersetzung")
                                .build()))
                    .originOfTranslations(
                        List.of(
                            OriginOfTranslation.builder()
                                .id(UUID.randomUUID())
                                .languageCode(
                                    LanguageCode.builder()
                                        .id(UUID.randomUUID())
                                        .label("Englisch")
                                        .isoCode("en")
                                        .isoCode3Letters("eng")
                                        .build())
                                .translators(List.of("Maxi Muster", "Ursel Meier"))
                                .borderNumbers(List.of(2L))
                                .urls(List.of("https://ihre-url-zur-englischen-übersetzung"))
                                .translationType(TranslationType.AMTLICH)
                                .build()))
                    .definitions(
                        List.of(
                            Definition.builder()
                                .definedTerm("indirekte Steuern")
                                .definingBorderNumber(2L)
                                .build(),
                            Definition.builder().definedTerm("Sachgesamtheit").build()))
                    .evsf("evsf test")
                    .appealAdmission(
                        AppealAdmission.builder().admitted(true).by(AppealAdmitter.FG).build())
                    .appeal(
                        Appeal.builder()
                            .appellants(
                                List.of(
                                    Appellant.builder().value("Kläger").build(),
                                    Appellant.builder().value("Beklagter").build()))
                            .revisionPlaintiffStatuses(
                                List.of(AppealStatus.builder().value("unzulässig").build()))
                            .revisionDefendantStatuses(
                                List.of(AppealStatus.builder().value("unzulässig").build()))
                            .jointRevisionPlaintiffStatuses(
                                List.of(AppealStatus.builder().value("unzulässig").build()))
                            .jointRevisionDefendantStatuses(
                                List.of(AppealStatus.builder().value("unzulässig").build()))
                            .nzbPlaintiffStatuses(
                                List.of(AppealStatus.builder().value("unzulässig").build()))
                            .nzbDefendantStatuses(
                                List.of(AppealStatus.builder().value("unzulässig").build()))
                            .appealWithdrawal(AppealWithdrawal.JA)
                            .pkhPlaintiff(PkhPlaintiff.NEIN)
                            .build())
                    .objectValues(
                        List.of(
                            ObjectValue.builder()
                                .currencyCode(
                                    CurrencyCode.builder()
                                        .id(UUID.randomUUID())
                                        .isoCode("EUR")
                                        .label("Euro (EUR)")
                                        .build())
                                .amount(9635)
                                .proceedingType(ProceedingType.ORGANSTREITVERFAHREN)
                                .build()))
                    .abuseFees(
                        List.of(
                            AbuseFee.builder()
                                .currencyCode(
                                    CurrencyCode.builder()
                                        .id(UUID.randomUUID())
                                        .isoCode("USD")
                                        .label("Dollar (USD)")
                                        .build())
                                .amount(1234)
                                .addressee(Addressee.BESCHWERDEFUEHRER_ANTRAGSTELLER)
                                .build()))
                    .countriesOfOrigin(
                        List.of(
                            CountryOfOrigin.builder()
                                .id(UUID.fromString("8bab996b-3e44-46c5-b588-52d4189d3da9"))
                                .legacyValue("legacy value")
                                .build(),
                            CountryOfOrigin.builder()
                                .id(UUID.fromString("5b202af2-6f77-47e0-8a9b-64e652845240"))
                                .country(
                                    FieldOfLaw.builder()
                                        .notation("RE-07-DEU")
                                        .text("Deutschland")
                                        .build())
                                .fieldOfLaw(
                                    FieldOfLaw.builder()
                                        .notation("AR-01-01-01")
                                        .text(
                                            "Verschulden bei Vertragsschluss (culpa in contrahendo)")
                                        .build())
                                .build()))
                    .relatedPendingProceedings(
                        List.of(
                            RelatedPendingProceeding.builder()
                                .uuid(UUID.fromString("b7f4d567-1ee4-4e72-b6d4-a1e56c77815a"))
                                .court(
                                    Court.builder()
                                        .type("AG")
                                        .location("Aachen")
                                        .jurisdictionType("Ordentliche Gerichtsbarkeit")
                                        .label("AG Aachen")
                                        .build())
                                .decisionDate(LocalDate.of(2022, 1, 23))
                                .documentNumber("XXRE011158825")
                                .documentType(
                                    DocumentType.builder().label("Anhängiges Verfahren").build())
                                .fileNumber("VIa ZR 339/22")
                                .build()))
                    .incomeTypes(
                        List.of(
                            IncomeType.builder()
                                .typeOfIncome(TypeOfIncome.ESTG)
                                .terminology("terminology test")
                                .build(),
                            IncomeType.builder().typeOfIncome(TypeOfIncome.GEWSTG).build()))
                    .nonApplicationNorms(
                        List.of(
                            NormReference.builder()
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("§ 30")
                                            .dateOfRelevance("2020")
                                            .dateOfVersion(LocalDate.of(2021, 2, 5))
                                            .build()))
                                .normAbbreviation(
                                    NormAbbreviation.builder().abbreviation("SeefBgV").build())
                                .build()))
                    .build())
            .previousDecisions(List.of(previousDecision1, previousDecision2))
            .ensuingDecisions(List.of(ensuingDecision1, ensuingDecision2))
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

  static Stream<Arguments> testCasesForIncludedAttributes() {
    return Stream.of(
        Arguments.of(
            "'documentNumber' (Dokumentnummer)",
            """
               <akn:FRBRthis value="YYTestDoc0013"/>
               """),
        // CoreData/Formaldaten -->
        Arguments.of(
            "'court' (Gericht)",
            """
                <ris:gericht domainTerm="Gericht"akn:refersTo="#gericht">
                  <ris:gerichtstyp domainTerm="Gerichtstyp">courtType</ris:gerichtstyp>
                  <ris:gerichtsort domainTerm="Gerichtsort">courtLocation</ris:gerichtsort>
                  <ris:spruchkoerper domainTerm="Spruchkörper" akn:refersTo="#spruchkoerper">appraisalBodytest</ris:spruchkoerper>
                </ris:gericht>
               """),
        Arguments.of(
            "'decisionDate' (Entscheidungsdatum)",
            """
                <akn:FRBRdate date="2020-01-01" name="Entscheidungsdatum"/>
               """),
        Arguments.of(
            "'documentType' (Dokumenttyp)",
            """
                <ris:dokumenttyp akn:eId="dokumenttyp" domainTerm="Dokumenttyp">documentType test</ris:dokumenttyp>
               """),
        Arguments.of(
            "'ecli'",
            """
               <akn:FRBRalias name="ECLI" value="ecli test"/>
               """),
        Arguments.of(
            "'fileNumber' (Aktenzeichen)",
            """
              <ris:aktenzeichenListe domainTerm="Liste der Aktenzeichen">
                <ris:aktenzeichen domainTerm="Aktenzeichen" akn:refersTo="#aktenzeichen">fileNumber test</ris:aktenzeichen>
              </ris:aktenzeichenListe>
              """),
        Arguments.of(
            "'appraisalBody/judicialBody' (Spruchkörper)",
            """
                <ris:spruchkoerper domainTerm="Spruchkörper" akn:refersTo="#spruchkoerper">appraisalBody test</ris:spruchkoerper>
               """),
        Arguments.of(
            "'region' (Region)",
            """
            <ris:regionen domainTerm="Regionen">
              <ris:region domainTerm="Region">NW</ris:region>
            </ris:regionen>
            """),
        // Normen -->
        Arguments.of(
            "'normReferences' (Normen)",
            """
               <akn:implicitReference ris:domainTerm="Norm">
                  <ris:norm domainTerm="Norm">
                     <ris:abkuerzung domainTerm="Abkürzung">normReference test</ris:abkuerzung>
                     <ris:einzelnorm domainTerm="Einzelnorm">
                        <ris:bezeichnung domainTerm="Bezeichnung">singleNorm test</ris:bezeichnung>
                        <ris:fassungsdatum domainTerm="Fassungsdatum">2021-02-05</ris:fassungsdatum>
                        <ris:jahr domainTerm="Jahr">2020</ris:jahr>
                     </ris:einzelnorm>
                     <ris:einzelnorm domainTerm="Einzelnorm">
                        <ris:bezeichnung domainTerm="Bezeichnung">singleNorm 2 test</ris:bezeichnung>
                        <ris:fassungsdatum domainTerm="Fassungsdatum">2022-03-06</ris:fassungsdatum>
                        <ris:jahr domainTerm="Jahr">2022</ris:jahr>
                     </ris:einzelnorm>
                  </ris:norm>
               """),
        // PreviousDecisions -->
        Arguments.of(
            "'previousDecisions' (Vorgehende Entscheidungen)",
            """
                 <akn:implicitReference ris:domainTerm="Rechtszug">
                     <ris:vorgehend domainTerm="Vorgehende Entscheidung">
                         <ris:dokumenttyp domainTerm="Dokumenttyp">previous decision document type</ris:dokumenttyp>
                         <ris:entscheidungsdatum domainTerm="Entscheidungsdatum">2020-01-01</ris:entscheidungsdatum>
                         <ris:dokumentnummer domainTerm="Dokumentnummer">previous decision document number 1</ris:dokumentnummer>
                         <ris:aktenzeichen domainTerm="Aktenzeichen">previous decision file number</ris:aktenzeichen>
                         <ris:gericht domainTerm="Gericht">
                             <ris:gerichtstyp domainTerm="Gerichtstyp">previous decision court type</ris:gerichtstyp>
                             <ris:gerichtsort domainTerm="Gerichtsort">previous decision court location</ris:gerichtsort>
                         </ris:gericht>
                     </ris:vorgehend>
                 </akn:implicitReference>
                 <akn:implicitReference ris:domainTerm="Rechtszug">
                     <ris:vorgehend domainTerm="Vorgehende Entscheidung">
                         <ris:dokumenttyp domainTerm="Dokumenttyp">previous decision document type</ris:dokumenttyp>
                         <ris:entscheidungsdatum domainTerm="Entscheidungsdatum">2020-01-01</ris:entscheidungsdatum>
                         <ris:dokumentnummer domainTerm="Dokumentnummer">previous decision document number 2</ris:dokumentnummer>
                         <ris:aktenzeichen domainTerm="Aktenzeichen">previous decision file number</ris:aktenzeichen>
                         <ris:gericht domainTerm="Gericht">
                             <ris:gerichtstyp domainTerm="Gerichtstyp">previous decision court type</ris:gerichtstyp>
                             <ris:gerichtsort domainTerm="Gerichtsort">previous decision court location</ris:gerichtsort>
                         </ris:gericht>
                     </ris:vorgehend>
                 </akn:implicitReference>
                 """),
        // EnsuingDecisions -->
        Arguments.of(
            "'ensuingDecisions' (Nachgehende Entscheidungen)",
            """
                <akn:implicitReference ris:domainTerm="Rechtszug">
                    <ris:nachgehend domainTerm="Nachgehende Entscheidung" art="anhängig">
                        <ris:dokumenttyp domainTerm="Dokumenttyp">ensuing decision document type</ris:dokumenttyp>
                        <ris:entscheidungsdatum domainTerm="Entscheidungsdatum">2022-10-01</ris:entscheidungsdatum>
                        <ris:dokumentnummer domainTerm="Dokumentnummer">ensuing decision document number 1</ris:dokumentnummer>
                        <ris:aktenzeichen domainTerm="Aktenzeichen">ensuing decision file number</ris:aktenzeichen>
                        <ris:gericht domainTerm="Gericht">
                            <ris:gerichtstyp domainTerm="Gerichtstyp">ensuing decision court type</ris:gerichtstyp>
                        </ris:gericht>
                    </ris:nachgehend>
                </akn:implicitReference>
                <akn:implicitReference ris:domainTerm="Rechtszug">
                    <ris:nachgehend domainTerm="Nachgehende Entscheidung" art="anhängig">
                        <ris:dokumenttyp domainTerm="Dokumenttyp">ensuing decision document type</ris:dokumenttyp>
                        <ris:entscheidungsdatum domainTerm="Entscheidungsdatum">2022-10-01</ris:entscheidungsdatum>
                        <ris:dokumentnummer domainTerm="Dokumentnummer">ensuing decision document number 2</ris:dokumentnummer>
                        <ris:aktenzeichen domainTerm="Aktenzeichen">ensuing decision file number</ris:aktenzeichen>
                        <ris:gericht domainTerm="Gericht">
                            <ris:gerichtstyp domainTerm="Gerichtstyp">ensuing decision court type</ris:gerichtstyp>
                        </ris:gericht>
                        <ris:vermerk domainTerm="Vermerk">ensuing decision note</ris:vermerk>
                    </ris:nachgehend>
                </akn:implicitReference>
                """),
        // LongTexts/Langtexte -->
        Arguments.of(
            "'dissentingOpinion' (Abweichende Meinung)",
            """
              <akn:motivation ris:domainTerm="Abweichende Meinung">
                  <akn:p>dissenting test</akn:p>
                  <akn:block name="Mitwirkende Richter">
                      <akn:opinion ris:domainTerm="Art der Mitwirkung" type="dissenting" by="#dr-phil-max-mustermann">referenced opinions test 1</akn:opinion>
                      <akn:opinion ris:domainTerm="Art der Mitwirkung" type="dissenting" by="#richterin-maxima-mustermann">referenced opinions test 2</akn:opinion>
                  </akn:block>
              </akn:motivation>
              """),
        Arguments.of(
            "'caseFacts' (Tatbestand)",
            """
                <akn:background ris:domainTerm="Tatbestand">
                  <akn:p>caseFacts test</akn:p>
                </akn:background>
               """),
        Arguments.of(
            "'decisionGrounds/decisionReasons' (Entscheidungsgründe)",
            """
            <akn:motivation ris:domainTerm="Entscheidungsgründe">
                <akn:p>decisionGrounds test</akn:p>
            </akn:motivation>
            """),
        Arguments.of(
            "'grounds/reasons' (Gründe)",
            """
            <akn:motivation ris:domainTerm="Gründe">
                <akn:p>grounds test</akn:p>
            </akn:motivation>
            """),
        Arguments.of(
            "'otherLongText' (Sonstiger Langtext)",
            """
            <akn:motivation ris:domainTerm="Sonstiger Langtext">
                <akn:p>otherLongText test</akn:p>
            </akn:motivation>
            """),
        Arguments.of(
            "'tenor' (Tenor)",
            """
            <akn:decision ris:domainTerm="Tenor">
                <akn:p>tenor test</akn:p>
            </akn:decision>
            """),
        Arguments.of(
            "'outline' (Gliederung)'",
            """
            <akn:introduction ris:domainTerm="Gliederung">
                <akn:p>outline test</akn:p>
            </akn:introduction>
            """),
        Arguments.of(
            "'guidingPrinciple' (Leitsatz)",
            """
            <akn:introduction ris:domainTerm="Leitsatz">
                <akn:p>guidingPrinciple test</akn:p>
            </akn:introduction>
            """),
        Arguments.of(
            "'headline' (Titelzeile)",
            """
                <akn:header>
                   <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">fileNumber test</akn:docNumber>
                   </akn:p>
                   <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01" refersTo="#entscheidungsdatum">01.01.2020</akn:docDate>
                   </akn:p>
                   <akn:p>Gericht: <akn:courtType refersTo="#gericht">courtLabel test</akn:courtType>
                   </akn:p>
                   <akn:p>Dokumenttyp: <akn:docType refersTo="#dokumenttyp">documentType test</akn:docType>
                   </akn:p>
                </akn:header>
               """));
  }

  static Stream<Arguments> testCasesForExcludedAttributes() {
    return Stream.of(
        Arguments.of("'decisionNames' (Entscheidungsnamen)", "ris:decisionNames"),
        Arguments.of("'keywords' (Schlagworte)", "akn:keyword"),
        Arguments.of("'headnote' (Orientierungssatz)", "headNote test"),
        Arguments.of("'inputType' (Eingangsart)", "inputType test"),
        Arguments.of("'legalEffect' (Rechtskraft)", "ris:legalEffect"),
        Arguments.of("'otherHeadNote' (Sonstiger Orientierungssatz)", "otherHeadNote test"),
        Arguments.of("'procedures' (Vorgänge)", "ris:procedures"),
        Arguments.of("'fieldsOfLaw' (Sachgebiete)", "ris:fieldOfLaws"),
        Arguments.of("'source' (Quelle)", "sourceRawValue test"),
        Arguments.of("'documentationOffice' (Dokumentationsstelle)", "ris:documentationOffice"),
        Arguments.of(
            "'creatingDocumentationOffice' (erstellende Dokumentationsstelle)",
            "creatingDocumentationOffice"),
        Arguments.of("'activeCitations' (Aktivzitierung)", "citation test"),
        Arguments.of("'deviatingCourts' (Abweichende Gerichte)", "ris:deviatingCourts"),
        Arguments.of("'deviatingDates' (Abweichende Entscheidungsdatum)", "ris:deviatingDates"),
        Arguments.of("'deviatingEclis' (Abweichende Eclis)", "ris:deviatingEclis"),
        Arguments.of(
            "'deviatingFileNumbers' (Abweichende Aktenzeichen)", "ris:deviatingFileNumbers"),
        Arguments.of(
            "'deviatingDocumentNumbers' (Abweichende Dokumentnummer)",
            "ris:deviatingDocumentNumbers"),
        Arguments.of("'berichtigung' (Berichtigungen)", "ris:berichtigungen"),
        Arguments.of("'herkunftslaender' (Herkunftsländer)", "ris:herkunftslaender"));
  }
}
