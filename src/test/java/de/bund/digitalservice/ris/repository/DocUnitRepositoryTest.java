package de.bund.digitalservice.ris.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataR2dbcTest
@Tag("test")
class DocUnitRepositoryTest {
  @Autowired DocUnitRepository docUnitRepo;

  @Autowired DatabaseClient client;

  @Test
  void testFindByFileType() {
    DocUnit docx = DocUnit.createNew();
    docx.setS3path("bucket1/originalA.docx");
    docx.setFiletype("docx");
    docUnitRepo.save(docx).subscribe();

    DocUnit pdf = DocUnit.createNew();
    pdf.setS3path("bucket1/originalB.pdf");
    pdf.setFiletype("pdf");
    docUnitRepo.save(pdf).subscribe();

    docUnitRepo.findByFileType("docx").as(StepVerifier::create).expectNextCount(1).verifyComplete();
  }

  @Test // as used in service.updateDocUnit()
  void testUpdateByOverSaving() {
    DocUnit docUnit = DocUnit.createNew();
    docUnitRepo.save(docUnit).subscribe();
    docUnitRepo.save(docUnit).subscribe();
    assertEquals(1, docUnitRepo.count().block());
  }
}
