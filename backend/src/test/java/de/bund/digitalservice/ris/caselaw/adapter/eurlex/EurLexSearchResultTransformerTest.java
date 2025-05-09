package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Slf4j
class EurLexSearchResultTransformerTest {
  @Test
  void transformEurLexResult2SearchResultList_loadAsDom()
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document doc =
        factory
            .newDocumentBuilder()
            .parse(this.getClass().getClassLoader().getResourceAsStream("eurlex-response.xml"));

    NodeList searchResultsList = doc.getElementsByTagName("searchResults");

    Page<SearchResult> searchResults1 =
        EurLexSearchResultTransformer.transformToDomain((Element) searchResultsList.item(0));

    Assertions.assertThat(searchResults1).hasSize(1);
  }
}
