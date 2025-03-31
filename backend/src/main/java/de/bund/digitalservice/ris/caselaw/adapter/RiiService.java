package de.bund.digitalservice.ris.caselaw.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RiiService {

  private static final String RII_URL = "https://www.rechtsprechung-im-internet.de/rii-toc.xml";

  public List<String> fetchRiiDocumentNumbers() {
    List<String> documentNumbers = new ArrayList<>();
    try {
      Document doc =
          Jsoup.connect(RII_URL).maxBodySize(0).parser(Parser.xmlParser()).timeout(15_000).get();

      Elements links = doc.select("link");

      for (Element link : links) {
        String linkText = link.text();
        String documentNumber = extractDocumentNumber(linkText);

        if (documentNumber != null) {
          documentNumbers.add(documentNumber);
        }
      }

      return documentNumbers;
    } catch (IOException e) {
      log.info(
          "Error fetching document numbers from Rechtsprechung im Internet: " + e.getMessage());
    }
    return List.of();
  }

  @SuppressWarnings("java:S5852")
  private static String extractDocumentNumber(String link) {
    String pattern = ".*/jb-([^/]+)\\.zip$";
    return link.matches(pattern) ? link.replaceAll(pattern, "$1") : null;
  }
}
