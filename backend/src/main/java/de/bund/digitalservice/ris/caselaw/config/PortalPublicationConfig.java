package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.FullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.ReducedLdmlTransformer;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PortalPublicationConfig {

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
