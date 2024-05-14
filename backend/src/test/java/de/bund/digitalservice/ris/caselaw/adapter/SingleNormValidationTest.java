package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.config.FeatureToggleConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.NormElement;
import de.bund.digitalservice.ris.caselaw.domain.NormElementRepository;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormCode;
import de.bund.digitalservice.ris.caselaw.domain.validator.SingleNormValidator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@ExtendWith(SpringExtension.class)
@Import({
  DocumentUnitService.class,
  SingleNormValidator.class,
  LocalValidatorFactoryBean.class,
  FeatureToggleConfig.class
})
class SingleNormValidationTest {

  @Autowired private DocumentUnitService service;
  @Autowired private Validator validator;
  @MockBean private DocumentUnitRepository repository;
  @MockBean private NormElementRepository normElementRepository;
  @MockBean private DocumentNumberService numberService;
  @MockBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocumentUnitStatusService statusService;
  @MockBean private PublicationReportRepository publicationReportRepository;
  @MockBean AttachmentService attachmentService;

  @BeforeEach
  void setUp() {
    when(normElementRepository.findAllByDocumentCategoryLabelR())
        .thenReturn(
            List.of(
                new NormElement("AB", true, null),
                new NormElement("CD", true, null),
                new NormElement("EF", false, null),
                new NormElement("GH", false, null),
                // Einigungsvertrag
                new NormElement("IJ", true, NormCode.EINIGUNGS_VERTRAG.name()),
                new NormElement("KL", true, NormCode.EINIGUNGS_VERTRAG.name()),
                new NormElement("MN", false, NormCode.EINIGUNGS_VERTRAG.name()),
                new NormElement("OP", false, NormCode.EINIGUNGS_VERTRAG.name())));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        ":",
        "AB XY:",
        "AB  XY:",
        "AB XYCD VW:",
        "EF:",
        "AB XY EF GH CD 12:",
        "EF GH:",
        ":EinigVtr",
        "IJ XY:EinigVtr",
        "MN:EinigVtr",
        "IJ XY OP MN KL 12: EinigVtr",
        "AB XY:blafasel",
        "AB XY 12 EF GH:",
        "GH:",
        "AB\u2007these\u202Fare\u2060special\uFEFFwhitespace\u00A0characters:"
      },
      delimiter = ':')
  void testValidate(String singleNorm, String normAbbreviation) {
    SingleNormValidationInfo singleNormValidationInfo =
        new SingleNormValidationInfo(singleNorm, normAbbreviation);

    var message = service.validateSingleNorm(singleNormValidationInfo);
    Assertions.assertEquals("Ok", message);
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "AB:",
        "EF XY:",
        "AB EF XY GH:",
        "MN OP:",
        "AB 1CD.2:",
        "ABXY:",
        "XY:",
        "IJ KL:EinigVtr",
        "IJ XY KL MN: EinigVtr",
        "AB XY: EinigVtr"
      },
      delimiter = ':')
  void testValidate_withValidationError(String singleNorm, String normAbbreviation) {
    SingleNormValidationInfo singleNormValidationInfo =
        new SingleNormValidationInfo(singleNorm, normAbbreviation);

    var message = service.validateSingleNorm(singleNormValidationInfo);
    Assertions.assertEquals("Validation error", message);
  }

  @Test
  void testValidate_withSingleNormIsNull() {
    SingleNormValidationInfo singleNormValidationInfo =
        new SingleNormValidationInfo(null, "norm abbreviation");

    var message = service.validateSingleNorm(singleNormValidationInfo);
    Assertions.assertEquals("Ok", message);
  }

  @Test
  void testValidate_withNormAbbreviationIsNull() {
    SingleNormValidationInfo singleNormValidationInfo = new SingleNormValidationInfo("AB 1", null);

    var message = service.validateSingleNorm(singleNormValidationInfo);
    Assertions.assertEquals("Ok", message);
  }

  @Test
  void testValidate_withSingleNormAndNormAbbreviationAreNull() {
    SingleNormValidationInfo singleNormValidationInfo = new SingleNormValidationInfo(null, null);

    var message = service.validateSingleNorm(singleNormValidationInfo);
    Assertions.assertEquals("Ok", message);
  }
}
