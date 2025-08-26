package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.event.KeyValuePair;
import org.springframework.core.env.Environment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PortalSanityCheckServiceTest {

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private PortalBucket portalBucket;
  @MockitoBean private RiiService riiService;
  @MockitoBean private PortalPublicationService portalPublicationService;
  @MockitoBean private Environment env;

  private PortalSanityCheckService subject;

  @BeforeEach
  void setUp() {
    subject =
        new PortalSanityCheckService(
            portalPublicationService, riiService, documentationUnitRepository, portalBucket, env);
  }

  @Test
  void testphaseSanityCheck_shouldDeleteDocumentNumbersInPortalButNotInRii() {
    when(env.matchesProfiles("production")).thenReturn(true);
    when(riiService.fetchRiiDocumentNumbers()).thenReturn(List.of("123", "456"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml", "789.xml"));

    subject.logPortalPublicationSanityCheck();

    verify(portalPublicationService).uploadDeletionChangelog(List.of("789.xml"));
  }

  @Test
  void testphaseSanityCheck_shouldNotRunOnOtherEnvs() {
    when(env.matchesProfiles("production")).thenReturn(false);
    when(riiService.fetchRiiDocumentNumbers()).thenReturn(List.of("123", "456"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml", "789.xml"));

    subject.logPortalPublicationSanityCheck();

    verify(portalPublicationService, times(0)).uploadDeletionChangelog(any());
  }

  @Test
  void sanityCheck_shouldFindDocumentsNotInDatabase() {
    when(env.matchesProfiles("production")).thenReturn(false);
    when(documentationUnitRepository.findAllPublishedDocumentNumbers())
        .thenReturn(Set.of("123", "456"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml", "789.xml"));

    TestMemoryAppender memoryAppender = new TestMemoryAppender(PortalSanityCheckService.class);

    subject.logPortalPublicationSanityCheck();

    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
    assertThat(memoryAppender.getMessage(Level.INFO, 0))
        .isEqualTo(
            "Finished sanity check for published doc units. Compared published doc units in db (status=PUBLISHED) and in bucket (LDML exists).");
    List<KeyValuePair> infoKVPairs = memoryAppender.getKeyValuePairs(Level.INFO, 0);
    assertThat(infoKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("inDatabaseNotInBucket", 0),
            new KeyValuePair("inBucketNotInDatabase", 1),
            new KeyValuePair("inDatabase", 2),
            new KeyValuePair("inBucket", 3));

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1L);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo("Published document numbers found in bucket but not in database");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(new KeyValuePair("inBucketNotInDatabase", "789"));
  }

  @Test
  void sanityCheck_shouldFindDocumentsNotInBucket() {
    when(env.matchesProfiles("production")).thenReturn(false);
    when(documentationUnitRepository.findAllPublishedDocumentNumbers())
        .thenReturn(Set.of("123", "456", "789"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml"));

    TestMemoryAppender memoryAppender = new TestMemoryAppender(PortalSanityCheckService.class);

    subject.logPortalPublicationSanityCheck();

    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
    assertThat(memoryAppender.getMessage(Level.INFO, 0))
        .isEqualTo(
            "Finished sanity check for published doc units. Compared published doc units in db (status=PUBLISHED) and in bucket (LDML exists).");
    List<KeyValuePair> infoKVPairs = memoryAppender.getKeyValuePairs(Level.INFO, 0);
    assertThat(infoKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("inDatabaseNotInBucket", 1),
            new KeyValuePair("inBucketNotInDatabase", 0),
            new KeyValuePair("inDatabase", 3),
            new KeyValuePair("inBucket", 2));

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1L);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo("Published document numbers found in database but not in bucket");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(new KeyValuePair("inDatabaseNotInBucket", "789"));
  }

  @Test
  void sanityCheck_shouldLogBothErrorsWhenBothMismatch() {
    when(env.matchesProfiles("production")).thenReturn(false);
    when(documentationUnitRepository.findAllPublishedDocumentNumbers())
        .thenReturn(Set.of("123", "789"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml"));

    TestMemoryAppender memoryAppender = new TestMemoryAppender(PortalSanityCheckService.class);

    subject.logPortalPublicationSanityCheck();

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(2L);
  }

  @Test
  void sanityCheck_shouldOnlyLogInfoWhenDatabaseAndBucketIsTheSame() {
    when(env.matchesProfiles("production")).thenReturn(false);
    when(documentationUnitRepository.findAllPublishedDocumentNumbers())
        .thenReturn(Set.of("123", "456", "789"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml", "789.xml"));

    TestMemoryAppender memoryAppender = new TestMemoryAppender(PortalSanityCheckService.class);

    subject.logPortalPublicationSanityCheck();

    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
    assertThat(memoryAppender.getMessage(Level.INFO, 0))
        .isEqualTo(
            "Finished sanity check for published doc units. Compared published doc units in db (status=PUBLISHED) and in bucket (LDML exists).");
    List<KeyValuePair> infoKVPairs = memoryAppender.getKeyValuePairs(Level.INFO, 0);
    assertThat(infoKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("inDatabaseNotInBucket", 0),
            new KeyValuePair("inBucketNotInDatabase", 0),
            new KeyValuePair("inDatabase", 3),
            new KeyValuePair("inBucket", 3));

    assertThat(memoryAppender.count(Level.ERROR)).isZero();
  }
}
