package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.Attachment2Html;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommonConverterService implements ConverterService {

  private final DocxConverterService docxConverterService;
  private final FmxConverterService fmxConverterService;

  public CommonConverterService(
      DocxConverterService docxConverterService, FmxConverterService fmxConverterService) {
    this.docxConverterService = docxConverterService;
    this.fmxConverterService = fmxConverterService;
  }

  /**
   * Convert attached file to an object with the html content of the file and potentially extracted
   * metadata.
   *
   * @param fileName name of the file in the bucket
   * @return the generated object with html content and metadata, or null if the filename is null or
   *     blank
   */
  public Attachment2Html getConvertedObject(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return null;
    }
    return docxConverterService.getConvertedObject(fileName);
  }

  /**
   * Convert attached file to an object with the html content of the file and potentially extracted
   * metadata.
   *
   * @param format file format of the attached file
   * @param fileName (optional) name of the file in the bucket in case of a bucket attachment
   * @param documentationUnitId (optional) id of the documentation unit that the file is attached to
   *     in case of a database attachment
   * @return the generated object with html content and metadata, or null if format and filename are
   *     null or blank
   */
  public Attachment2Html getConvertedObject(
      String format, String fileName, UUID documentationUnitId) {
    if ("fmx".equals(format)) {
      return fmxConverterService.getFmx(documentationUnitId);
    } else {
      return docxConverterService.getConvertedObject(fileName);
    }
  }
}
