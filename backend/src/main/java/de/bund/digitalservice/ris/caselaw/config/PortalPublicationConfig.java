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
      DocumentBuilderFactory documentBuilderFactory,
      PrototypePortalBucket prototypePortalBucket,
      ObjectMapper objectMapper,
      RiiService riiService) {
    return new PrototypePortalPublicationService(
        documentationUnitRepository,
        xmlUtilService,
        documentBuilderFactory,
        prototypePortalBucket,
        objectMapper,
        riiService);
  }

  @Bean
  @Profile({"staging"})
  public PortalPublicationService stagingPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      PortalBucket portalBucket,
      ObjectMapper objectMapper) {
    return new StagingPortalPublicationService(
        documentationUnitRepository,
        xmlUtilService,
        documentBuilderFactory,
        portalBucket,
        objectMapper);
  }

  @Bean
  @Profile({"!production", "!staging"})
  public PortalPublicationService noOpPortalPublicationService() {
    return new NoOpPortalPublicationService();
  }
}
