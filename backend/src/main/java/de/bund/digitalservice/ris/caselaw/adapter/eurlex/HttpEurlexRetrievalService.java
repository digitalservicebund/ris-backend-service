package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.adapter.exception.FmxImporterException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpEurlexRetrievalService implements EurlexRetrievalService {

  @SuppressWarnings("java:S2142")
  @Override
  public String requestEurlexResultList(String url, String payload) {
    try {
      HttpClient client = HttpClient.newBuilder().build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(url))
              .POST(HttpRequest.BodyPublishers.ofString(payload))
              .header("Content-Type", "application/soap+xml")
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    } catch (IOException | InterruptedException | URISyntaxException ex) {
      log.error("Can't get search results from EUR-Lex webservice.", ex);
      throw new EurLexSearchException(ex);
    }
  }

  @SuppressWarnings({"java:S2142", "java:S5042"})
  @Override
  public String requestSingleEurlexDocument(String sourceUrl) {
    String fmxFileContent = null;

    try {
      HttpClient client =
          HttpClient.newBuilder()
              .followRedirects(HttpClient.Redirect.ALWAYS) // follow redirects
              .build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(sourceUrl))
              .GET()
              .header("Accept", "application/zip;mtype=fmx4")
              .header("Accept-Language", "de")
              .build();

      HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
      ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(response.body()));

      ZipEntry entry;

      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.getName().endsWith(".xml")) {
          fmxFileContent = new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
      }
    } catch (IOException | InterruptedException | URISyntaxException ex) {
      throw new FmxImporterException("Downloading FMX file from Eurlex Database failed.", ex);
    }

    if (fmxFileContent != null && !fmxFileContent.isBlank()) {
      return fmxFileContent;
    }
    return null;
  }
}
