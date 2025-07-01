package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.NoOpPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.PortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.PrototypePortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.PrototypePortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.RiiService;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DecisionPortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DecisionPrototypePortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PortalPublicationConfig {

  @Bean
  @Profile({"production"})
  public PortalPublicationService prototypePortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      PrototypePortalBucket prototypePortalBucket,
      ObjectMapper objectMapper,
      de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer portalTransformer,
      RiiService riiService) {
    return new PrototypePortalPublicationService(
        documentationUnitRepository,
        xmlUtilService,
        prototypePortalBucket,
        objectMapper,
        portalTransformer,
        riiService);
  }

  @Bean
  @Profile({"staging", "local"})
  public PortalPublicationService stagingPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      PortalBucket portalBucket,
      ObjectMapper objectMapper,
      de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer portalTransformer) {
    return new StagingPortalPublicationService(
        documentationUnitRepository, xmlUtilService, portalBucket, objectMapper, portalTransformer);
  }

  @Bean
  @Profile({"!production & !staging & !local"})
  public PortalPublicationService noOpPortalPublicationService() {
    return new NoOpPortalPublicationService();
  }

  @Bean
  @Profile({"production"})
  public de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer prototypePortalTransformer(
      DocumentBuilderFactory documentBuilderFactory) {
    return new DecisionPrototypePortalTransformer(documentBuilderFactory);
  }

  @Bean
  @Profile({"staging", "local"})
  public de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer stagingPortalTransformer(
      DocumentBuilderFactory documentBuilderFactory) {
    return new DecisionPortalTransformer(documentBuilderFactory);
  }

  @Bean
  @Profile({"!staging & !production & !local"})
  public de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer defaultPortalTransformer(
      DocumentBuilderFactory documentBuilderFactory) {
    return new DecisionPrototypePortalTransformer(documentBuilderFactory);
  }
}
