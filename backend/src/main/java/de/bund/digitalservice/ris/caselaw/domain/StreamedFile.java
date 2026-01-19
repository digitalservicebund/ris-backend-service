package de.bund.digitalservice.ris.caselaw.domain;

import java.io.IOException;
import java.io.InputStream;

public record StreamedFile(
    InputStream inputStream, long contentLength, String contentType, String filename, String eTag)
    implements AutoCloseable {

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
