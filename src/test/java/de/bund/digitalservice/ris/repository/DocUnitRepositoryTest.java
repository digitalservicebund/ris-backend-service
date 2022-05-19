package de.bund.digitalservice.ris.repository;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

@SpringBootTest(properties = { "otc.obs.bucket-name=testBucket" })
@Tag("test")
class DocUnitRepositoryTest {
  @Autowired DocUnitRepository docUnitRepo;

  @Autowired DatabaseClient client;

  @Test
  void testFindByFileType() {
    DocUnit docx = new DocUnit();
    docx.setS3path("bucket1/originalA.docx");
    docx.setFiletype("docx");
    docUnitRepo.save(docx).subscribe();

    DocUnit pdf = new DocUnit();
    pdf.setS3path("bucket1/originalB.pdf");
    pdf.setFiletype("pdf");
    docUnitRepo.save(pdf).subscribe();

    docUnitRepo.findByFileType("docx").as(StepVerifier::create).expectNextCount(1).verifyComplete();
  }
}
