package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import de.bund.digitalservice.ris.caselaw.domain.fmx.Fmx2Html;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FmxConverterService {

  private final FmxRepository fmxRepository;
  private final Templates fmxToHtml;

  public FmxConverterService(FmxRepository fmxRepository, XmlUtilService xmlUtilService) {
    this.fmxRepository = fmxRepository;
    fmxToHtml = xmlUtilService.getTemplates("xml/fmxToHtml.xslt");
  }

  public Fmx2Html getFmx(UUID documentationUnitId) {
    try {
      Transformer xsltTransformer = fmxToHtml.newTransformer();
      String content = fmxRepository.getFmxAsString(documentationUnitId);
      if (content == null || content.isEmpty()) {
        return Fmx2Html.EMPTY;
      }
      StringWriter xsltOutput = new StringWriter();
      xsltTransformer.transform(
          new StreamSource(new StringReader(content.strip())), new StreamResult(xsltOutput));
      log.info(xsltOutput.toString());
      return new Fmx2Html(xsltOutput.toString());
    } catch (TransformerException e) {
      log.error("Xslt transformation error.", e);
      throw new MappingException(e.getMessage());
    }
  }
}
