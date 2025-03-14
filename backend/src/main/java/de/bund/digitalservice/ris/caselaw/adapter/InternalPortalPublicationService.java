package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.InternalPortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InternalPortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final InternalPortalBucket internalPortalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final InternalPortalTransformer ldmlTransformer;

  @Autowired
  public InternalPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      InternalPortalBucket internalPortalBucket,
      ObjectMapper objectMapper) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.internalPortalBucket = internalPortalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = new InternalPortalTransformer(documentBuilderFactory);
  }

  /**
   * Publish the documentation unit by transforming it to valid LDML and putting the resulting XML
   * file into a bucket together with a changelog file, specifying which documentation unit has been
   * added or updated.
   *
   * @param documentationUnitId the id of the documentation unit that should be published
   * @throws DocumentationUnitNotExistsException if the documentation unit with the given id could
   *     not be found in the database
   * @throws LdmlTransformationException if the documentation unit could not be transformed to valid
   *     LDML
   * @throws PublishException if the changelog file could not be created or either of the files
   *     could not be saved in the bucket
   */
  public void publishDocumentationUnit(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {

    Documentable documentable = documentationUnitRepository.findByUuid(documentationUnitId);

    if (!(documentable instanceof DocumentationUnit documentationUnit)) {
      throw new UnsupportedOperationException(
          "Publish not supported for Documentable type: " + documentable.getClass());
    }

    CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);

    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException(
          "Could not transform documentation unit to valid LDML.", null);
    }

    Changelog changelog = new Changelog(List.of(ldml.getUniqueId()), null);
    String changelogJson;
    try {
      changelogJson = objectMapper.writeValueAsString(changelog);
    } catch (IOException e) {
      log.error("Could not write changelog file. {}", e.getMessage());
      throw new PublishException(
          "Could not publish documentation unit to portal, because changelog file could not be created.",
          null);
    }

    try {
      internalPortalBucket.save(
          "changelogs/" + DateUtils.toDateTimeString(LocalDateTime.now()) + ".json", changelogJson);
    } catch (BucketException e) {
      log.error("Could not save changelog to bucket", e);
      throw new PublishException("Could not save changelog to bucket.", e);
    }

    try {
      internalPortalBucket.save(ldml.getUniqueId() + ".xml", fileContent.get());
      log.info("LDML for documentation unit {} successfully published.", ldml.getUniqueId());
    } catch (BucketException e) {
      log.error("Could not save LDML to bucket", e);
      throw new PublishException("Could not save LDML to bucket.", e);
    }
  }

  @Async
  @SuppressWarnings("java:S135")
  public void exportSampleLdmls() throws IOException {
    List<String> documentNumbers =
        List.of(
            "KORE300422021",
            "KORE300962024",
            "KORE300712021",
            "KORE315152024",
            "KORE300452019",
            "KORE303732016",
            "KORE317912010",
            "KORE629592018",
            "KORE313312019",
            "KORE307272022",
            "KVRE427971801",
            "KVRE417211601",
            "KVRE450362201",
            "KVRE443042101",
            "KVRE407641401",
            "KVRE457652301",
            "KVRE402711301",
            "KVRE451122301",
            "KVRE400071201",
            "KVRE438112001",
            "",
            "WBRE201800180",
            "WBRE201900148",
            "WBRE201900147",
            "WBRE201800214",
            "WBRE201800217",
            "WBRE201800218",
            "WBRE201800216",
            "WBRE201800215",
            "WBRE201800307",
            "WBRE202300156",
            "",
            "STRE201150211",
            "STRE201350061",
            "STRE201450500",
            "STRE201750064",
            "STRE201550401",
            "STRE201350278",
            "STRE201250108",
            "STRE201250718",
            "STRE201350075",
            "STRE201650167",
            "",
            "KSRE126071509",
            "KSRE125951515",
            "KSRE125801515",
            "KSRE125961515",
            "KSRE128321509",
            "KSRE130411615",
            "KSRE166401506",
            "KSRE144180209",
            "KSRE131851609",
            "KSRE130181715",
            "",
            "KARE600032200",
            "KARE600035228",
            "KARE600051335",
            "KARE600051463",
            "KARE600055529",
            "KARE600055896",
            "KARE600055891",
            "KARE600055897",
            "KARE600061633",
            "KARE600061634",
            "",
            "JURE229030565",
            "JURE169016439",
            "JURE229030432",
            "JURE239031156",
            "JURE209002984",
            "JURE239030994",
            "JURE249031620",
            "JURE219029935",
            "JURE199002514",
            "JURE249031399");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    for (String documentNumber : documentNumbers) {
      Documentable documentable;
      try {
        documentable = documentationUnitRepository.findByDocumentNumber(documentNumber);
      } catch (Exception ex) {
        log.error(
            "Couldn't export (step get documentation unit) {} as LegalDocML", documentNumber, ex);
        continue;
      }

      if (!(documentable instanceof DocumentationUnit documentationUnit)) {
        throw new UnsupportedOperationException(
            "Export not supported for Documentable type: " + documentable.getClass());
      }

      CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);

      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      if (fileContent.isEmpty()) {
        log.error("Couldn't export (step generate file content) {} as LegalDocML", documentNumber);
        continue;
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.get().getBytes());

      byte[] bytes = new byte[1024];
      int length;

      ZipEntry entry = new ZipEntry(documentationUnit.documentNumber() + ".xml");
      zos.putNextEntry(entry);

      while ((length = bais.read(bytes)) >= 0) {
        zos.write(bytes, 0, length);
      }

      zos.closeEntry();

      log.info("Add {} to the zip file.", documentationUnit.documentNumber());
    }

    zos.close();
    ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
    buffer.rewind();

    internalPortalBucket.saveBytes("test_documentation_units.zip", buffer);
  }
}
