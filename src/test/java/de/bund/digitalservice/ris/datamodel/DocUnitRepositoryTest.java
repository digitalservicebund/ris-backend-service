package de.bund.digitalservice.ris.datamodel;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

@SpringBootTest
class DocUnitRepositoryTest {

  @Autowired DocUnitRepository docUnitRepo;

  @Autowired DatabaseClient client;

  @BeforeEach
  public void setup() {
    Arrays.asList(
            "DROP TABLE IF EXISTS DOC_UNIT;",
            "CREATE table DOC_UNIT (id INT AUTO_INCREMENT NOT NULL, s3path VARCHAR2, filetype VARCHAR2 NOT NULL);")
        .forEach(
            command ->
                client
                    .sql(command)
                    .fetch()
                    .rowsUpdated()
                    .as(StepVerifier::create)
                    .expectNextCount(1)
                    .verifyComplete());
  }

  @Test
  void testFindByFileType() {
    DocUnit docx = new DocUnit();
    docx.s3path = "bucket1/originalA.docx";
    docx.filetype = "docx";
    docUnitRepo.save(docx).subscribe();

    DocUnit pdf = new DocUnit();
    pdf.s3path = "bucket1/originalB.pdf";
    pdf.filetype = "pdf";
    docUnitRepo.save(pdf).subscribe();

    docUnitRepo.findByFileType("docx").as(StepVerifier::create).expectNextCount(1).verifyComplete();
  }
}
