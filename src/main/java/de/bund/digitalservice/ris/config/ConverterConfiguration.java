package de.bund.digitalservice.ris.config;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfiguration {
  @Bean
  public DocumentBuilderFactory documentBuilderFactory() {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (ParserConfigurationException e) {
      throw new BeanCreationException(
          "DocumentBuilderFactory", "Couldn't create DocumentBuilderFactory!", e);
    }
    return dbFactory;
  }
}
