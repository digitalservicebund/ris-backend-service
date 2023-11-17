package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
class JurisXmlExporterWrapperIntegrationTest {

  private JurisXmlExporter jurisXmlExporter;
  private String encryptedXml;
  private ObjectMapper objectMapper;
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String documentNr = "ABCDE20220001";

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Test
  void test_validDocumentUnit_shouldReturnEncryptedXMLString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of("fileNumber1", "fileNumber2"))
            .deviatingFileNumbers(List.of("deviatingFileNumber1", "deviatingFileNumber2"))
            .court(new Court("courtType", null, null, null))
            .ecli("ecli")
            .deviatingEclis(List.of("dev-ecli-1", "dev-ecli-2"))
            .documentType(
                DocumentType.builder().jurisShortcut("category").label("category123").build())
            .ecli("ecli")
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
            .deviatingDecisionDates(
                List.of(
                    Instant.parse("2021-01-01T00:00:00Z"), Instant.parse("2021-01-02T00:00:00Z")))
            .build();

    Texts texts = Texts.builder().decisionName("decisionName").build();

    List<ProceedingDecision> proceedingDecisions =
        List.of(
            ProceedingDecision.builder()
                .uuid(UUID.randomUUID())
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .decisionDate(Instant.parse("2020-05-06T00:00:00Z"))
                .dateKnown(true)
                .fileNumber("fileNumber")
                .documentType(
                    DocumentType.builder().jurisShortcut("category").label("category123").build())
                .build());

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .coreData(coreData)
            .proceedingDecisions(proceedingDecisions)
            .texts(texts)
            .build();

    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals(
        "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj7n5CUPU7L1LxgQOake4IDv3U9KN0Ucqu3G1lFcZMlZcU6HuBHlFojDNxw/GOxqumrLLqoTKsMI/c5vHmQuwQt36ss7raPKw2wf6dM7GfHGeheclptyuXQgfSBrMwXVJv7kwfY5dWJ1+FJxBQsl4gDDdoXkCil9wJeZeOTKXFI0zOQUC2m19Q5+ro8jE3RKMi5LbLncOv1UJaG/F4S5I1WxRvswpWXDkj6jXPg61okdtggCQ/t1a0So3OAru/xVM8WItbYH16u5uN41/E34zQC1A7z0eVEI9N1QOyur30k7blYFtdNYhFsVQrJgOKUWhNgzt4gbqJq+wxh38kQd3/DDuJnMM6KFUI0nTsurW3FMWLGty9o/hMR+CWOgDG0ZoD6o4SW9jp8iDNvSpBeHPBRy66IbGlz/tDdmbBHn0IyW3Yw/v8tF9bYOCDFnRgybLo2ZB5rgyvgEceBfBdCknDr4mpfW1AM+nP46BIYg9uU47o/35ZPwHfjL+jY7fR3uhaa0gaDQAFCEjmV7VRB9OQ/3Aqb832kdiRNs4iQ52nGo929PwwMM27D+XFIL5BPjEuLWnkn2WoMrbf9x1GCKFK59qIjEUsHYYblzeij0BRMwm8KMVQ+FqBNLaoDBpLoU7S/BEvD1KYgDgNS7mZ9jQMtabAAXUIgtopMJlm0vNXkGOEb/PmnumHRM6SP1DIRMRafj8gtBupPi+gOTh5k2GBNPqCAVIgipLVTt3a9winZcYC5D5qz4C44zxmwGaymelXql6qQWaYwuY66Arc30vmyMU6LQSMain0I9li5DlRLGBhMk2eD+S9o2YPk7o0hrj3n+9HlQTiILLDseX6iWSn2nFtuU3NzEkwPNmeV4WTQdO+kUP6l4UTGpJcBi3JP3qbbuwQqrkq9oFcMPQi4i0yLWdDQ07NlbQn4/t8XygvwCeyeCFbQuZYplSYen7j1NGQRK273ZE8uQ5gs45exV/P3K308LnwBOShIpveWhIzF2AzXI7YRiFshcdW2H1q4FOby9A4obIvuMsOiTtrw5hzXAfnxceJ98yDLYKnTfgc8E6myWl4Ub+gB8OP2HmKiUvmiuE+SKfxMBoWNk8132Ttgcy3nYeqdQGdgHVrLAfVwfDZ+JSPyMlDvVaFGA9QdEbBMnE5ux3mEzIHaz8zKkbqWNzJw/TV1MUsRO7ylOSE4qdgBLxOn81hyY00T8uHAKxxxEgDU2LWps+gwceqsWEgb1z0wv2NK3EUaVRerRJXhJJp8c4OVaoXOj7jcCrvECft3mWYDrSsYS5px8IpJAcU4xslSaxf3DJ6CTQdWiSkHD1+hQ4P3S702NRnsMkl55PatJNzLtVmMDFW2h512X5K2R24Ne0a93FQuYIwEhB8Ws2lFMEJ94OEzlYVCRauXEL6JQcskLNBfyZQcjk8oiIYtiiQt4B8E8P472QomK8/J+C88sCwLUzVh9D5RqPxSWC5nxiTn3cleBEEzuBWNUqYYGZ49C+Chtfd7vG/gXepm0EDIuK8IFIFotar5REt4y03IrTRYsYZPMVVI4y67KFQP7D/y/3L3hY2xLguY3U46PRnsNwAVSeUakkfuAGXxbzYDr5BTg+S32IbRSfMYBH/rPL2+z6/tMzZglAdwNSr9ttlRdLA10VDqieldQ+aS7f3HmWYDrSsYS5px8IpJAcU4yEb+qjRozQJZH2UvBQpSZ1IKpS1ohRVCYdUv+tZK3t1EEDo4twezrxcvbV+MBHOOVgXm1WkYfKlZYhZL1b/s/aQ2bD0FL2WavWOOE1evnzHsE6vj9iP7D2qQc4SljY00WZ49C+Chtfd7vG/gXepm0Eu37CpBVg7tfBzbwevHOO/h4RHaJzXDnxpuLpGF//Lp1nq0lA6Pl+yCv5vQxXBATD1hks1/QLDIU7AHh/nAF9XJDodAefzH0ijNInVJZ3XjpD8/e1mw6uSxzVzMuZIi6Z0nccJNnufHx1qmIVzc0tq8ShYwsMCieqM6KNwz+1agGDsn4sr0FU7DB6FIgqi4kjlKbN2N5aPnTqtaKYznrxHw+fsmm3KmiHZPzLsmXh18uYm4NTLEK4IXiYWMwbMPK3L0Dihsi+4yw6JO2vDmHNcNScekmBxgaVz3oXODNog6m63MbhS/0ky7mw/MxWwJlcFEee8vL4ahiAaCoo4PU98XjMSYkS1ab2d9S0w/R87b7KKJq/4r54gtfgnPdma+Nn",
        encryptedXml);
  }

  @Test
  void test_invalidDocumentUnitMissingMandatoryFields_shouldReturnEmptyString() throws Exception {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .creationtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .build();

    encryptedXml = jurisXmlExporter.generateEncryptedXMLString(documentUnit);

    assertEquals("", encryptedXml);
  }

  // In order to be in sync with the model, all possible fields are set in this
  // test documentUnit and checked for null fields. Everytime a new field is
  // added to the data model this test will fail, as it is not set in the test
  // documentUnit. The test documentUnit needs to be updated accordingly to be
  // in sync with the model.
  @Test
  void testDocumentUnitIsSyncedWithModel()
      throws NoSuchFieldException,
          SecurityException,
          IllegalArgumentException,
          IllegalAccessException {
    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of("fileNumber"))
            .documentType(DocumentType.builder().jurisShortcut("ca").label("category").build())
            .procedure(Procedure.builder().label("procedure").build())
            .ecli("ecli")
            .deviatingEclis(List.of("dev-ecli-1", "dev-ecli-2"))
            .appraisalBody("appraisalBody")
            .decisionDate(Instant.parse("2021-01-01T00:00:00Z"))
            .legalEffect("legalEffect")
            .inputType("inputType")
            .documentationOffice(
                DocumentationOffice.builder().label("fooOffice").label("FO").build())
            .region("region")
            .build();
    Texts texts =
        Texts.builder()
            .decisionName("decisionName")
            .headline("headline")
            .guidingPrinciple("guidingPrinciple")
            .headnote("headnote")
            .tenor("tenor")
            .reasons("reasons")
            .caseFacts("caseFacts")
            .decisionReasons("decisionReasons")
            .build();

    List<ProceedingDecision> proceedingDecisions =
        List.of(
            ProceedingDecision.builder()
                .uuid(UUID.randomUUID())
                .documentNumber("documentNumber")
                .dataSource(DataSource.NEURIS)
                .court(new Court("courtType", "courtPlace", "courtLabel", null))
                .decisionDate(Instant.parse("2020-04-05T00:00:00Z"))
                .dateKnown(true)
                .fileNumber("fileNumber")
                .documentType(
                    DocumentType.builder().jurisShortcut("category").label("category123").build())
                .build());

    ContentRelatedIndexing indexing =
        ContentRelatedIndexing.builder()
            .keywords(List.of("keyword1", "keyword2"))
            .fieldsOfLaw(
                List.of(
                    FieldOfLaw.builder()
                        .id(1L)
                        .identifier("SF-01")
                        .text("field of law text")
                        .build()))
            .norms(List.of(DocumentUnitNorm.builder().singleNorm("01").build()))
            .build();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber(documentNr)
            .creationtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .fileuploadtimestamp(Instant.parse("2021-01-01T00:00:00Z"))
            .dataSource(DataSource.NEURIS)
            .s3path("s3path")
            .filetype("filetype")
            .filename("filename")
            .coreData(coreData)
            .proceedingDecisions(proceedingDecisions)
            .texts(texts)
            .contentRelatedIndexing(indexing)
            .status(
                DocumentUnitStatus.builder()
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .build())
            .build();

    assertThat(documentUnit).hasNoNullFieldsOrProperties();

    for (Field field : documentUnit.getClass().getDeclaredFields()) {
      if (field.getType().equals(CoreData.class) || field.getType().equals(Texts.class))
        assertThat(field).hasNoNullFieldsOrProperties();
      if (field.getType().equals(List.class)) {
        field.setAccessible(true);
        List<ProceedingDecision> previousDecisionsList =
            (List<ProceedingDecision>) field.get(documentUnit);
        for (ProceedingDecision proceedingDecision : previousDecisionsList) {
          assertThat(proceedingDecision).hasNoNullFieldsOrProperties();
        }
      }
    }
  }
}
