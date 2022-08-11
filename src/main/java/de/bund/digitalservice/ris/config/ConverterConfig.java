package de.bund.digitalservice.ris.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import de.bund.digitalservice.ris.utils.DocxConverter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ConverterConfig {
  @Bean
  public DocxConverter docxConverter() {
    return new DocxConverter();
  }

  @Bean
  public DocumentBuilderFactory documentBuilderFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch (ParserConfigurationException e) {
      throw new BeanCreationException(
          "DocumentBuilderFactory", "Couldn't create DocumentBuilderFactory!", e);
    }

    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    return factory;
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Bean
  public JurisXmlExporter jurisXmlExporter() {
    return new JurisXmlExporter(objectMapper());
  }
}
