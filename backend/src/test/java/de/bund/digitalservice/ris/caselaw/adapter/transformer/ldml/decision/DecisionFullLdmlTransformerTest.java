package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.Notation;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.TestUtils;
import de.bund.digitalservice.ris.caselaw.domain.AbuseFee;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.Addressee;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmission;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmitter;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreement;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustry;
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
import java.io.IOException;
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
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

@ExtendWith(MockitoExtension.class)
class DecisionFullLdmlTransformerTest {

  private static Decision testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static DecisionFullLdmlTransformer subject;
  private static UUID documentationUnitId;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    subject = new DecisionFullLdmlTransformer(documentBuilderFactory);

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
        Decision.builder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .ecli("testecli")
                    .court(
                        Court.builder()
                            .type("AG")
                            .location("Aachen")
                            .jurisdictionType("Ordentliche Gerichtsbarkeit")
                            .label("AG Aachen")
                            .build())
                    .documentType(
                        DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                    .legalEffect("Ja")
                    .fileNumbers(List.of("testFileNumber"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .build())
            .documentNumber("testDocumentNumber")
            .longTexts(LongTexts.builder().caseFacts("<p>Example content 1</p>").build())
            .shortTexts(ShortTexts.builder().build())
            .previousDecisions(List.of(related1, related2))
            .build();
  }

  @Nested
  class DissentingOpinion {
    @Test
    void testDissentingOpinion_withoutParticipatingJudges() {
      String expected =
          """
          <akn:motivation ris:domainTerm="Abweichende Meinung">
            <akn:p>dissenting test</akn:p>
          </akn:motivation>
         """;
      Decision dissentingCaseLaw =
          testDocumentUnit.toBuilder()
              .longTexts(
                  testDocumentUnit.longTexts().toBuilder()
                      .dissentingOpinion("<p>dissenting test</p>")
                      .build())
              .build();
      CaseLawLdml ldml = subject.transformToLdml(dissentingCaseLaw);
      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      Assertions.assertTrue(fileContent.isPresent());
      Assertions.assertTrue(
          StringUtils.deleteWhitespace(fileContent.get())
              .contains(StringUtils.deleteWhitespace(expected)));
    }

    @Test
    void testDissentingOpinion_withParticipatingJudgeWithoutReferencedOpinion() {
      String expected =
          """
          <akn:motivation ris:domainTerm="Abweichende Meinung">
            <akn:p>dissenting test</akn:p>
            <akn:block name="Mitwirkende Richter">
               <akn:opinion by="#maxi-musterfrau"/>
            </akn:block>
          </akn:motivation>
         """;
      Decision dissentingCaseLaw =
          testDocumentUnit.toBuilder()
              .longTexts(
                  testDocumentUnit.longTexts().toBuilder()
                      .dissentingOpinion("<p>dissenting test</p>")
                      .participatingJudges(
                          List.of(ParticipatingJudge.builder().name("Maxi Musterfrau").build()))
                      .build())
              .build();
      CaseLawLdml ldml = subject.transformToLdml(dissentingCaseLaw);
      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      Assertions.assertTrue(fileContent.isPresent());
      Assertions.assertTrue(
          StringUtils.deleteWhitespace(fileContent.get())
              .contains(StringUtils.deleteWhitespace(expected)));
    }

    @Test
    void testDissentingOpinion_withParticipatingJudgesWithReferencedOpinions() {
      String expected =
          """
         <akn:motivation ris:domainTerm="Abweichende Meinung">
            <akn:p>dissenting test</akn:p>
            <akn:block name="Mitwirkende Richter">
               <akn:opinion ris:domainTerm="Art der Mitwirkung"
                            type="dissenting"
                            by="#maxi-gaertner">Art der Mitwirkung 1</akn:opinion>
               <akn:opinion ris:domainTerm="Art der Mitwirkung"
                            type="dissenting"
                            by="#herbert-guenter">Art der Mitwirkung 2</akn:opinion>
            </akn:block>
         </akn:motivation>
         """;
      Decision dissentingCaseLaw =
          testDocumentUnit.toBuilder()
              .longTexts(
                  testDocumentUnit.longTexts().toBuilder()
                      .dissentingOpinion("<p>dissenting test</p>")
                      .participatingJudges(
                          List.of(
                              ParticipatingJudge.builder()
                                  .name("Maxi Gärtner")
                                  .referencedOpinions("Art der Mitwirkung 1")
                                  .build(),
                              ParticipatingJudge.builder()
                                  .name("Herbert Günter")
                                  .referencedOpinions("Art der Mitwirkung 2")
                                  .build()))
                      .build())
              .build();
      CaseLawLdml ldml = subject.transformToLdml(dissentingCaseLaw);
      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      Assertions.assertTrue(fileContent.isPresent());
      Assertions.assertTrue(
          StringUtils.deleteWhitespace(fileContent.get())
              .contains(StringUtils.deleteWhitespace(expected)));
    }

    @Test
    void testWithoutDissentingOpinionAndWithParticipatingJudges_shouldNotBuildDissentingOpinion() {
      String expected =
          """
              <akn:judgmentBody>
                <akn:background ris:domainTerm="Tatbestand">
                   <akn:p>Example content 1</akn:p>
                </akn:background>
             </akn:judgmentBody>
         """;
      Decision dissentingCaseLaw =
          testDocumentUnit.toBuilder()
              .longTexts(
                  testDocumentUnit.longTexts().toBuilder()
                      .participatingJudges(
                          List.of(
                              ParticipatingJudge.builder()
                                  .name("Maxi Gärtner")
                                  .referencedOpinions("Art der Mitwirkung 1")
                                  .build(),
                              ParticipatingJudge.builder()
                                  .name("Herbert Günter")
                                  .referencedOpinions("Art der Mitwirkung 2")
                                  .build()))
                      .build())
              .build();
      CaseLawLdml ldml = subject.transformToLdml(dissentingCaseLaw);
      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      Assertions.assertTrue(fileContent.isPresent());
      Assertions.assertTrue(
          StringUtils.deleteWhitespace(fileContent.get())
              .contains(StringUtils.deleteWhitespace(expected)));
    }
  }

  @Test
  @DisplayName("Headnote test")
  void headnoteTest() {
    String expected =
        """
          <ris:dokumentarischeKurztexte domainTerm="Dokumentarische Kurztexte">
             <ris:orientierungssatz ris:domainTerm="Orientierungssatz">
                <akn:p>headnote test</akn:p>
             </ris:orientierungssatz>
          </ris:dokumentarischeKurztexte>
        """;
    Decision headnoteCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(
                testDocumentUnit.shortTexts().toBuilder().headnote("<p>headnote test</p>").build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(headnoteCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("OtherHeadnote test")
  void otherHeadnoteTest() {
    String expected =
        """
          <ris:dokumentarischeKurztexte domainTerm="Dokumentarische Kurztexte">
             <ris:sonstigerOrientierungssatz ris:domainTerm="Sonstiger Orientierungssatz">
                <akn:p>other headnote test</akn:p>
             </ris:sonstigerOrientierungssatz>
          </ris:dokumentarischeKurztexte>
         """;
    Decision otherHeadnoteCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(
                testDocumentUnit.shortTexts().toBuilder()
                    .otherHeadnote("<p>other headnote test</p>")
                    .build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(otherHeadnoteCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("Grounds test")
  void groundTest() {
    String expected =
        """
         <akn:motivation ris:domainTerm="Gründe">
            <akn:p>grounds test</akn:p>
         </akn:motivation>
         """;
    Decision groundsCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                testDocumentUnit.longTexts().toBuilder().reasons("<p>grounds test</p>").build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(groundsCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("OtherLongText without main decision test")
  void otherLongTextWithoutMainDecisionTest() {
    String expected =
        """
         <akn:motivation ris:domainTerm="Sonstiger Langtext">
            <akn:p>Other long text test</akn:p>
         </akn:motivation>
         """;
    Decision otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                testDocumentUnit.longTexts().toBuilder()
                    .otherLongText("<p>Other long text test</p>")
                    .build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("Long text with non breaking spaces")
  void testTransformToLdml_longTextWithNBSP_shouldReplaceItWithUnicode() {
    String expected =
        """
         <akn:motivation ris:domainTerm="Gründe">
            <akn:p>text with non breaking spaces</akn:p>
         </akn:motivation>
         """;
    Decision otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                LongTexts.builder()
                    .reasons("<p>text with non&nbsp;breaking&nbsp;spaces</p>")
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Non self closing break inside block element")
  void testTransform_borderNumber() {
    String expected =
        """
        <akn:hcontainer ris:domainTerm="Randnummer" eId="randnummer-1" name="Randnummer">
           <akn:num>1</akn:num>
           <akn:content>
             <akn:p>Lorem ipsum</akn:p>
           </akn:content>
        </akn:hcontainer>
        """;
    Decision otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                LongTexts.builder()
                    .reasons(
                        """
                        <border-number>
                            <number>1</number>
                            <content>
                                <p>Lorem ipsum</p>
                            </content>
                        </border-number>
                        """)
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  void testTransform_borderNumberLink() {
    String expected =
        """
        <akn:p>Übertragungsleistungssteuerungsverfahren</akn:p>
        <akn:p>
          This is my guiding principle<akn:ref ris:domainTerm="Randnummernverlinkung" class="border-number-link" href="#randnummer-70">70</akn:ref>
        </akn:p>
        """;
    Decision decision =
        testDocumentUnit.toBuilder()
            .shortTexts(
                ShortTexts.builder()
                    .guidingPrinciple(
                        """
                        <p>Übertragungsleistungssteuerungsverfahren</p>
                        <p>This is my guiding principle<border-number-link nr="70">70</border-number-link></p>
                        """)
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(decision);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Long text with <ignore-once> tags>")
  void testTransformToLdml_longTextWithIgnoreOnceTags_shouldRemoveTags() {
    String expected =
        """
              <akn:judgmentBody>
                 <akn:motivation ris:domainTerm="Entscheidungsgründe">
                    <akn:p>text with ignored spell check issue</akn:p>
                 </akn:motivation>
              </akn:judgmentBody>
             """;
    Decision otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                LongTexts.builder()
                    .decisionReasons(
                        "<p>text with <ignore-once>ignored</ignore-once> spell check issue</p>")
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Nested
  class CourtTest {
    @Test
    void testTransform_withSuperiorCourt_shouldNotIncludeGerichtsOrt() {
      String expectedReferences =
          """
          <akn:references source="#ris">
              <akn:TLCOrganization eId="ris" href="" showAs="Rechtsinformationssystem des Bundes"/>
              <akn:TLCOrganization eId="gericht" href="" showAs="BGH"/>
          </akn:references>
         """;
      String expectedRisMeta =
          """
          <ris:gericht domainTerm="Gericht" akn:refersTo="#gericht">
             <ris:gerichtstyp domainTerm="Gerichtstyp">BGH</ris:gerichtstyp>
             <ris:gerichtsbarkeit domainTerm="Gerichtsbarkeit">Ordentliche Gerichtsbarkeit</ris:gerichtsbarkeit>
         </ris:gericht>
         """;
      Decision decision =
          testDocumentUnit.toBuilder()
              .coreData(
                  CoreData.builder()
                      .court(
                          Court.builder()
                              .isSuperiorCourt(true)
                              .isForeignCourt(false)
                              .label("BGH")
                              .type("BGH")
                              .regions(List.of("DEU"))
                              .jurisdictionType("Ordentliche Gerichtsbarkeit")
                              .build())
                      .documentType(
                          DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                      .fileNumbers(List.of("testFileNumber"))
                      .decisionDate(LocalDate.of(2020, 1, 1))
                      .build())
              .build();

      CaseLawLdml ldml = subject.transformToLdml(decision);

      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      assertThat(fileContent).isPresent();
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expectedReferences));
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expectedRisMeta));
    }

    @Test
    void testTransform_withNullCourtLocation_shouldNotIncludeGerichtsOrt() {
      String expectedReferences =
          """
          <akn:references source="#ris">
              <akn:TLCOrganization eId="ris" href="" showAs="Rechtsinformationssystem des Bundes"/>
              <akn:TLCOrganization eId="gericht" href="" showAs="Tribunal Economico-Administrativo Regional Katalonien"/>
          </akn:references>
         """;
      String expectedRisMeta =
          """
          <ris:gericht domainTerm="Gericht" akn:refersTo="#gericht">
             <ris:gerichtstyp domainTerm="Gerichtstyp">Tribunal Economico-Administrativo Regional Katalonien</ris:gerichtstyp>
         </ris:gericht>
         """;
      Decision decision =
          testDocumentUnit.toBuilder()
              .coreData(
                  CoreData.builder()
                      .court(
                          Court.builder()
                              .isSuperiorCourt(false)
                              .isForeignCourt(true)
                              .label("Tribunal Economico-Administrativo Regional Katalonien")
                              .type("Tribunal Economico-Administrativo Regional Katalonien")
                              .jurisdictionType("")
                              .build())
                      .documentType(
                          DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                      .fileNumbers(List.of("testFileNumber"))
                      .decisionDate(LocalDate.of(2020, 1, 1))
                      .build())
              .build();

      CaseLawLdml ldml = subject.transformToLdml(decision);

      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      assertThat(fileContent).isPresent();
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expectedReferences));
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expectedRisMeta));
    }

    @Test
    void testTransform_withWithCourtLocation_shouldIncludeGerichtsOrt() {
      String expectedReferences =
          """
          <akn:references source="#ris">
              <akn:TLCOrganization eId="ris" href="" showAs="Rechtsinformationssystem des Bundes"/>
              <akn:TLCOrganization eId="gericht" href="" showAs="AG Aachen"/>
              <akn:TLCLocation eId="gerichtsort" href="" showAs="Aachen"/>
          </akn:references>
         """;
      String expectedRisMeta =
          """
          <ris:gericht domainTerm="Gericht" akn:refersTo="#gericht">
             <ris:gerichtstyp domainTerm="Gerichtstyp">AG</ris:gerichtstyp>
             <ris:gerichtsort domainTerm="Gerichtsort">Aachen</ris:gerichtsort>
             <ris:gerichtsbarkeit domainTerm="Gerichtsbarkeit">Ordentliche Gerichtsbarkeit</ris:gerichtsbarkeit>
         </ris:gericht>
         """;
      Decision decision =
          testDocumentUnit.toBuilder()
              .coreData(
                  CoreData.builder()
                      .court(
                          Court.builder()
                              .isSuperiorCourt(false)
                              .isForeignCourt(false)
                              .label("AG Aachen")
                              .type("AG")
                              .location("Aachen")
                              .regions(List.of("NW"))
                              .jurisdictionType("Ordentliche Gerichtsbarkeit")
                              .build())
                      .documentType(
                          DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                      .fileNumbers(List.of("testFileNumber"))
                      .decisionDate(LocalDate.of(2020, 1, 1))
                      .build())
              .build();

      CaseLawLdml ldml = subject.transformToLdml(decision);

      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      assertThat(fileContent).isPresent();
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expectedReferences));
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expectedRisMeta));
    }
  }

  @Nested
  class Header {
    @Test
    @DisplayName("Mixed text in header")
    void testTransform_mixedTextInHeader() {
      String expected =
          """
         <akn:header>
            <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">testFileNumber</akn:docNumber>
            </akn:p>
            <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01" refersTo="#entscheidungsdatum">01.01.2020</akn:docDate>
            </akn:p>
            <akn:p>Gericht: <akn:courtType refersTo="#gericht">AG Aachen</akn:courtType>
            </akn:p>
            <akn:p>Dokumenttyp: <akn:docType refersTo="#dokumenttyp">testDocumentTypeAbbreviation</akn:docType></akn:p>
            <akn:p>Entscheidungsnamen:
            <akn:docTitle refersTo="#entscheidungsname">Entscheidungsname</akn:docTitle></akn:p>
            <akn:p>Kurztitel:
            <akn:shortTitle refersTo="#titelzeile">
              <akn:embeddedStructure>
                <akn:p alternativeTo="textWrapper">Hello</akn:p>
                <akn:p> paragraph</akn:p>
                <akn:p alternativeTo="textWrapper"> world!</akn:p>
              </akn:embeddedStructure>
            </akn:shortTitle></akn:p>
          </akn:header>
         """;
      Decision otherLongTextCaseLaw =
          testDocumentUnit.toBuilder()
              .shortTexts(
                  ShortTexts.builder()
                      .decisionNames(List.of("Entscheidungsname"))
                      .headline("Hello<p> paragraph</p> world!")
                      .build())
              .build();

      CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

      Assertions.assertNotNull(ldml);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      assertThat(fileContent).isPresent();
      assertThat(StringUtils.deleteWhitespace(fileContent.get()))
          .contains(StringUtils.deleteWhitespace(expected));
    }

    @Nested
    class ShortTitle {
      @Test
      void testTransform_withoutShortTitle_shouldUseFallback() {
        String expected =
            """
              <akn:header>
                <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">testFileNumber</akn:docNumber></akn:p>
                <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01"refersTo="#entscheidungsdatum">01.01.2020</akn:docDate></akn:p>
                <akn:p>Gericht:<akn:courtType refersTo="#gericht">AG Aachen</akn:courtType></akn:p>
                <akn:p>Dokumenttyp:<akn:doc Type refersTo="#dokumenttyp">testDocumentTypeAbbreviation</akn:docType></akn:p>
                <akn:p>Entscheidungsnamen:
                <akn:docTitle refersTo="#entscheidungsname">Entscheidungsname</akn:docTitle></akn:p>
                <akn:p>Kurztitel: <akn:shortTitle>
                   <akn:embeddedStructure>
                      <akn:p alternativeTo="textWrapper">AG Aachen, 01.01.2020, testFileNumber</akn:p>
                   </akn:embeddedStructure>
                  </akn:shortTitle>
                 </akn:p>
              </akn:header>
            """;
        Decision otherLongTextCaseLaw =
            testDocumentUnit.toBuilder()
                .shortTexts(
                    ShortTexts.builder().decisionNames(List.of("Entscheidungsname")).build())
                .build();

        CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

        assertThat(ldml).isNotNull();
        Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
        assertThat(fileContent).isPresent();
        assertThat(StringUtils.deleteWhitespace(fileContent.get()))
            .contains(StringUtils.deleteWhitespace(expected));
      }

      @Test
      void testTransform_withShortTitleInBrackets_shouldRemoveBrackets() {
        String expected =
            """
              <akn:header>
                <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">testFileNumber</akn:docNumber></akn:p>
                <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01"refersTo="#entscheidungsdatum">01.01.2020</akn:docDate></akn:p>
                <akn:p>Gericht:<akn:courtType refersTo="#gericht">AG Aachen</akn:courtType></akn:p>
                <akn:p>Dokumenttyp:<akn:doc Type refersTo="#dokumenttyp">testDocumentTypeAbbreviation</akn:docType></akn:p>
                <akn:p>Kurztitel: <akn:shortTitle refersTo="#titelzeile">
                   <akn:embeddedStructure>
                      <akn:p alternativeTo="textWrapper">This is a title in brackets!</akn:p>
                   </akn:embeddedStructure>
                  </akn:shortTitle>
                 </akn:p>
              </akn:header>
            """;
        Decision otherLongTextCaseLaw =
            testDocumentUnit.toBuilder()
                .shortTexts(ShortTexts.builder().headline("(This is a title in brackets!)").build())
                .build();

        CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

        assertThat(ldml).isNotNull();
        Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
        assertThat(fileContent).isPresent();
        assertThat(StringUtils.deleteWhitespace(fileContent.get()))
            .contains(StringUtils.deleteWhitespace(expected));
      }

      @Test
      void testTransform_withShortTitleInInlineBrackets_shouldNotRemoveBrackets() {
        String expected =
            """
              <akn:header>
                <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">testFileNumber</akn:docNumber></akn:p>
                <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01"refersTo="#entscheidungsdatum">01.01.2020</akn:docDate></akn:p>
                <akn:p>Gericht:<akn:courtType refersTo="#gericht">AG Aachen</akn:courtType></akn:p>
                <akn:p>Dokumenttyp:<akn:doc Type refersTo="#dokumenttyp">testDocumentTypeAbbreviation</akn:docType></akn:p>
                <akn:p>Kurztitel: <akn:shortTitle refersTo="#titelzeile">
                   <akn:embeddedStructure>
                      <akn:p alternativeTo="textWrapper">(This is a title) (with in line in brackets!)</akn:p>
                   </akn:embeddedStructure>
                  </akn:shortTitle>
                 </akn:p>
              </akn:header>
            """;
        Decision otherLongTextCaseLaw =
            testDocumentUnit.toBuilder()
                .shortTexts(
                    ShortTexts.builder()
                        .headline("(This is a title) (with in line in brackets!)")
                        .build())
                .build();

        CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

        assertThat(ldml).isNotNull();
        Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
        assertThat(fileContent).isPresent();
        assertThat(StringUtils.deleteWhitespace(fileContent.get()))
            .contains(StringUtils.deleteWhitespace(expected));
      }
    }
  }

  @Test
  @DisplayName("Keywords")
  void testTransform_keywords() {
    String expected =
        """
      <akn:keyword ris:domainTerm="Schlagwort" dictionary="" showAs="keyword 1" value="keyword 1"/>
      <akn:keyword ris:domainTerm="Schlagwort" dictionary="" showAs="Keyword 2" value="Keyword 2"/>
    """;
    Decision otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .keywords(List.of("keyword 1", "Keyword 2"))
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("hasDeliveryDate")
  void testTransform_hasDeliveryDate() {
    String expectedHeader =
"""
  <akn:p>Datum der Zustellung an Verkündungs statt: <akn:docDate date="2020-01-01" refersTo="#datum-der-zustellung-an-verkuendungs-statt">01.01.2020</akn:docDate>
  </akn:p>
""";
    String expectedFRBRdate =
"""
  <akn:FRBRdate date="2020-01-01" name="Datum der Zustellung an Verkündungs statt"/>
""";
    Decision deliveryDateCaseLaw =
        testDocumentUnit.toBuilder()
            .coreData(testDocumentUnit.coreData().toBuilder().hasDeliveryDate(true).build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(deliveryDateCaseLaw);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expectedHeader))
        .contains(StringUtils.deleteWhitespace(expectedFRBRdate));
  }

  static Stream<Arguments> provideTagFormattingTestCases() {
    return Stream.of(
        // Image with closing tag should only keep last path part (/my/path/to/img.png -> image.png)
        Arguments.of(
            "<p><img alt=\"alt text\" height=\"70\" src=\"path/to/image\" /></p>",
            "<akn:p><akn:imgsrc=\"image\"alt=\"alttext\"height=\"70\"/></akn:p>"),
        // Image without closing tag
        Arguments.of(
            "<p><img alt=\"alt text\" height=\"70\" src=\"path/to/image\"></p>",
            "<akn:p><akn:img src=\"image\" alt=\"alt text\" height=\"70\" /></akn:p>"),
        // Self-closing break not inside block element
        // Arguments.of("<br />", "<akn:p><akn:br/></akn:p>"),
        // Self-closing break inside block element
        Arguments.of("<p><br /></p>", "<akn:p><akn:br/></akn:p>"),
        // Non-self-closing break not inside block element
        // Arguments.of("<br>", "<akn:p><akn:br/></akn:p>"),
        // Non-self-closing break inside block element
        Arguments.of("<p><br></p>", "<akn:p><akn:br/></akn:p>"),
        // Col-groups in tables
        Arguments.of(
            "<table><colgroup><col><col></colgroup><tbody><tr><td>text</td><td>text</td></tr></tbody></table>",
            "<ris:table><ris:tbody><ris:tr><ris:td>text</ris:td><ris:td>text</ris:td></ris:tr></ris:tbody></ris:table>"));
  }

  @ParameterizedTest
  @MethodSource("provideTagFormattingTestCases")
  @DisplayName("Transform self closing HTML tags to LDML")
  void testTransformTags(String inputHtml, String expectedFragment) {
    Decision otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(LongTexts.builder().reasons(inputHtml).build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expectedFragment));
  }

  @Test
  void testEntireLdml() throws IOException {
    // Arrange
    var documentationUnit = getEntireDocumentationUnit();
    Path expectedFilePath = Paths.get("src/test/resources/testdata/decision_full_ldml.xml");
    String expected = Files.readString(expectedFilePath, StandardCharsets.UTF_8);

    // Act
    CaseLawLdml ldml = subject.transformToLdml(documentationUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();

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
      System.out.println("Transformed LDML:");
      System.out.println(fileContent.get());
      Assertions.fail("XMLs differ:\n" + differences);
    }
  }

  @Test
  void testMinimalLdml_withMandatoryFields_shouldThrowMissingJudgmentBody() {
    // Arrange
    var documentationUnit =

        // Use empty DTO as basis as it creates empty lists for everything.
        DecisionTransformer.transformToDomain(new DecisionDTO()).toBuilder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .court(Court.builder().type("AG").location("Aachen").label("AG Aachen").build())
                    .documentType(
                        DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                    .fileNumbers(List.of("testFileNumber"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .build())
            .documentNumber("testDocumentNumber")
            .build();

    // Act + Assert
    AssertionsForClassTypes.assertThatThrownBy(() -> subject.transformToLdml(documentationUnit))
        .isInstanceOf(LdmlTransformationException.class)
        .hasMessageContaining("Missing judgment body.");
  }

  @Test
  void testMinimalLdml_withMandatoryFieldsAndLongText() throws IOException {
    // Arrange
    var documentationUnit =
        // Use empty DTO as basis as it creates empty lists for everything.
        DecisionTransformer.transformToDomain(new DecisionDTO()).toBuilder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .isSuperiorCourt(false)
                            .isForeignCourt(true)
                            .type("Tribunal Administratif")
                            .location("Nantes")
                            .label("Tribunal Administratif Nantes")
                            .regions(List.of("FRA"))
                            .build())
                    .documentType(
                        DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                    .fileNumbers(List.of("testFileNumber"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .build())
            .documentNumber("testDocumentNumber")
            .longTexts(LongTexts.builder().caseFacts("<p>Example content 1</p>").build())
            .build();
    Path expectedFilePath = Paths.get("src/test/resources/testdata/decision_minimal_ldml.xml");
    String expected = Files.readString(expectedFilePath, StandardCharsets.UTF_8);

    // Act
    CaseLawLdml ldml = subject.transformToLdml(documentationUnit);

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

  Decision getEntireDocumentationUnit() {
    PreviousDecision previousDecision1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(
                Court.builder()
                    .type("previous decision court type")
                    .location("previous decision court location")
                    .jurisdictionType("previous decision jurisdiction type")
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
            .build();
    EnsuingDecision ensuingDecision2 =
        ensuingDecision1.toBuilder()
            .documentNumber("ensuing decision document number 2")
            .pending(true)
            .note("ensuing decision note")
            .build();

    // Use empty DTO as basis as it creates empty lists for everything.
    return DecisionTransformer.transformToDomain(new DecisionDTO()).toBuilder()
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
                        .abbreviation("creatingDocumentationOffice test")
                        .build())
                .court(
                    Court.builder()
                        .isSuperiorCourt(false)
                        .isForeignCourt(false)
                        .label("courtLabel test")
                        .type("courtType")
                        .location("courtLocation")
                        .jurisdictionType("jurisdictionType")
                        .regions(List.of("NW"))
                        .build())
                .courtBranchLocation(
                    CourtBranchLocation.builder().value("court branch location").build())
                .sources(
                    List.of(
                        Source.builder()
                            .sourceRawValue("sourceRawValue test")
                            .value(SourceValue.S)
                            .build(),
                        Source.builder().sourceRawValue("sourceRawValue test 2").build()))
                .documentType(DocumentType.builder().label("documentType test").build())
                .legalEffect("Ja")
                .fileNumbers(List.of("fileNumber test"))
                .decisionDate(LocalDate.of(2020, 1, 1))
                .oralHearingDates(List.of(LocalDate.of(2021, 2, 3), LocalDate.of(2020, 1, 2)))
                .appraisalBody("appraisalBody test")
                .inputTypes(List.of("E-Mail", "Papier"))
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
                .deviatingEclis(List.of("deviating ecli test"))
                .deviatingCourts(List.of("deviating court 1", "deviating court 2"))
                .deviatingFileNumbers(List.of("deviating fileNumber"))
                .deviatingDocumentNumbers(List.of("deviating documentNumber"))
                .deviatingDecisionDates(List.of(LocalDate.of(2010, 5, 12)))
                .yearsOfDispute(List.of(Year.of(2024)))
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
                        Correction.builder().type(CorrectionType.BERICHTIGUNGSBESCHLUSS).build(),
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
                .decisionNames(List.of("decisionName test", "decisionName2 test"))
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
                            .text("fieldOfLaw test")
                            .notation(Notation.NEW.toString())
                            .identifier("AR-01-01-01")
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
                                                        .abbreviation("legalForceType abbreviation")
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
                        CollectiveAgreement.builder()
                            .name("Stehende Bühnen")
                            .date("12.2001")
                            .norm("§ 23")
                            .industry(
                                new CollectiveAgreementIndustry(
                                    UUID.fromString("fa5800ee-1102-42e1-a516-4356e6232a41"),
                                    "Bühne, Theater, Orchester"))
                            .build()))
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
                                    .notation(Notation.NEW.toString())
                                    .identifier("RE-07-DEU")
                                    .text("Deutschland")
                                    .build())
                            .fieldOfLaw(
                                FieldOfLaw.builder()
                                    .notation(Notation.NEW.toString())
                                    .identifier("AR-01-01-01")
                                    .text("Verschulden bei Vertragsschluss (culpa in contrahendo)")
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
                                NormAbbreviation.builder()
                                    .abbreviation("SeefBgV")
                                    .officialLongTitle("OfficialLongTitle")
                                    .build())
                            .build()))
                .build())
        .previousDecisions(List.of(previousDecision1, previousDecision2))
        .ensuingDecisions(List.of(ensuingDecision1, ensuingDecision2))
        .caselawReferences(
            List.of(
                Reference.builder()
                    .referenceType(ReferenceType.CASELAW)
                    .citation("citation")
                    .primaryReference(true)
                    .referenceSupplement("reference supplement")
                    .legalPeriodical(
                        LegalPeriodical.builder()
                            .abbreviation("LegalPeriodical abbreviation")
                            .title("LegalPeriodical Title")
                            .subtitle("LegalPeriodical Subtitle")
                            .build())
                    .legalPeriodicalRawValue("LegalPeriodical")
                    .build()))
        .literatureReferences(
            List.of(
                Reference.builder()
                    .referenceType(ReferenceType.LITERATURE)
                    .author("author")
                    .citation("citation")
                    .primaryReference(false)
                    .legalPeriodicalRawValue("LegalPeriodical RawValue")
                    .documentType(DocumentType.builder().label("doc type").build())
                    .build()))
        .build();
  }
}
