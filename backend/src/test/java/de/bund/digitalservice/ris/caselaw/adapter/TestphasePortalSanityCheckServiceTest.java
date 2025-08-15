package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TestphasePortalSanityCheckServiceTest {

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private PortalBucket portalBucket;
  @MockitoBean private RiiService riiService;
  @MockitoBean private PortalPublicationService portalPublicationService;

  private TestphasePortalSanityCheckService subject;

  @BeforeEach
  void setUp() {
    subject =
        new TestphasePortalSanityCheckService(
            portalPublicationService, riiService, documentationUnitRepository, portalBucket);
  }

  @Test
  void sanityCheck_shouldDeleteDocumentNumbersInPortalButNotInRii() throws JsonProcessingException {
    when(riiService.fetchRiiDocumentNumbers()).thenReturn(List.of("123", "456"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml", "789.xml"));

    subject.logPortalPublicationSanityCheck();

    verify(portalPublicationService).uploadDeletionChangelog(List.of("789.xml"));
  }
}
