package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.NoOpPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.PortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.PrototypePortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.PrototypePortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.RiiService;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.FullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.ReducedLdmlTransformer;
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
      PortalTransformer portalTransformer,
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
      PortalTransformer portalTransformer) {
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
  public PortalTransformer prototypePortalTransformer(
      DocumentBuilderFactory documentBuilderFactory) {
    return new ReducedLdmlTransformer(documentBuilderFactory);
  }

  @Bean
  @Profile({"staging", "local"})
  public PortalTransformer stagingPortalTransformer(DocumentBuilderFactory documentBuilderFactory) {
    return new FullLdmlTransformer(documentBuilderFactory);
  }

  @Bean
  @Profile({"!staging & !production & !local"})
  public PortalTransformer defaultPortalTransformer(DocumentBuilderFactory documentBuilderFactory) {
    return new ReducedLdmlTransformer(documentBuilderFactory);
  }
}
