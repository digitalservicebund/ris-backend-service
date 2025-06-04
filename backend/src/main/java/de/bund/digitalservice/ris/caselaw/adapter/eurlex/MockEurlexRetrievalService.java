package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

public class MockEurlexRetrievalService implements EurlexRetrievalService {

  @SuppressWarnings("java:S2142")
  @Override
  public String requestEurlexResultList(String url, String payload) {
    try {
      ClassPathResource xsltResource = new ClassPathResource("testdata/eurlex.xml");
      return IOUtils.toString(xsltResource.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings({"java:S2142", "java:S5042"})
  @Override
  public String requestSingleEurlexDocument(String sourceUrl) {
    try {
      String path;
      if (sourceUrl.contains("62019CV0001")) {
        path = "testdata/62019CV0001_02.xml";
      } else if (sourceUrl.contains("62024CO0878")) {
        path = "testdata/62024CO0878.xml";
      } else {
        path = "testdata/62023CJ0538.xml";
      }
      ClassPathResource xsltResource = new ClassPathResource(path);
      return IOUtils.toString(xsltResource.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
