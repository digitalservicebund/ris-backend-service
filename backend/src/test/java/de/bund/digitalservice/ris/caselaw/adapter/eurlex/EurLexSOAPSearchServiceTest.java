package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({EurLexSOAPSearchService.class})
class EurLexSOAPSearchServiceTest {
  @MockitoBean private EurLexResultRepository repository;

  @MockitoBean private DatabaseCourtRepository courtRepository;

  @MockitoSpyBean private EurLexSOAPSearchService service;

  @Test
  void
      testGetSearchResults_withAnEmptyDocumentationOffice_shouldNotCallRepositoryRequestAndShouldReturnEmpty() {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();

    Page<SearchResult> searchResults =
        service.getSearchResults(
            "0",
            documentationOffice,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    assertThat(searchResults).isEmpty();
    verify(repository, never()).findAllBySearchParameters(any(), any(), any(), any(), any(), any());
  }

  @Test
  void
      testGetSearchResults_withDocumentationOfficeIsNull_shouldNotCallRepositoryRequestAndShouldReturnEmpty() {
    Page<SearchResult> searchResults =
        service.getSearchResults(
            "0",
            null,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    assertThat(searchResults).isEmpty();
    verify(repository, never()).findAllBySearchParameters(any(), any(), any(), any(), any(), any());
  }

  @Test
  void
      testGetSearchResults_withDocumentationOfficeNotAllowed_shouldNotCallRepositoryRequestAndShouldReturnEmpty() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("not allowed doc office").build();
    Page<SearchResult> searchResults =
        service.getSearchResults(
            "0",
            documentationOffice,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    assertThat(searchResults).isEmpty();
    verify(repository, never()).findAllBySearchParameters(any(), any(), any(), any(), any(), any());
  }

  @Test
  void testGetSearchResults_withoutPage_shouldCallRepositoryRequestWithPageZero() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("DS").build();
    when(repository.findAllBySearchParameters(
            PageRequest.of(0, 100),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()))
        .thenReturn(Page.empty());

    Page<SearchResult> searchResults =
        service.getSearchResults(
            null,
            documentationOffice,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    assertThat(searchResults).isEmpty();
    verify(repository, times(1))
        .findAllBySearchParameters(
            PageRequest.of(0, 100),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
  }

  @Test
  void
      testGetSearchResults_withBGHDocumentationOfficeAndNoCourtParameter_shouldReturnOnlyEuGDecisions() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("BGH").build();
    when(repository.findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuG")),
            eq(Optional.empty()),
            eq(Optional.empty())))
        .thenReturn(Page.empty());

    service.getSearchResults(
        "0",
        documentationOffice,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    verify(repository, times(1))
        .findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuG")),
            eq(Optional.empty()),
            eq(Optional.empty()));
  }

  @Test
  void
      testGetSearchResults_withBFHDocumentationOfficeAndNoCourtParameter_shouldReturnOnlyEuGHDecisions() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("BFH").build();
    when(repository.findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuGH")),
            eq(Optional.empty()),
            eq(Optional.empty())))
        .thenReturn(Page.empty());

    service.getSearchResults(
        "0",
        documentationOffice,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    verify(repository, times(1))
        .findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuGH")),
            eq(Optional.empty()),
            eq(Optional.empty()));
  }

  @Test
  void
      testGetSearchResults_withBFHDocumentationOfficeAndCourtParameterSetToEuG_shouldReturnOnlyEuGDecisions() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("BFH").build();
    when(repository.findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuG")),
            eq(Optional.empty()),
            eq(Optional.empty())))
        .thenReturn(Page.empty());

    service.getSearchResults(
        "0",
        documentationOffice,
        Optional.empty(),
        Optional.empty(),
        Optional.of("EuG"),
        Optional.empty(),
        Optional.empty());

    verify(repository, times(1))
        .findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuG")),
            eq(Optional.empty()),
            eq(Optional.empty()));
  }

  @Test
  void testGetSearchResults_lastUpdate5MinutesBefore_shouldNotCallRequestNewDecisions() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("DS").build();
    when(repository.findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty())))
        .thenReturn(Page.empty());
    Instant fiveMinutesBefore = Instant.now().minus(5, ChronoUnit.MINUTES);
    EurLexResultDTO lastUpdate = EurLexResultDTO.builder().createdAt(fiveMinutesBefore).build();
    when(repository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(lastUpdate));
    doNothing().when(service).requestNewestDecisions(anyInt(), any(LocalDate.class));

    service.getSearchResults(
        "0",
        documentationOffice,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    verify(service, never()).requestNewestDecisions(anyInt(), any(LocalDate.class));
  }

  @Test
  void testGetSearchResults_lastUpdate2DaysBefore_shouldCallRequestNewDecisions() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("DS").build();
    when(repository.findAllBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty())))
        .thenReturn(Page.empty());
    Instant twoDaysBefore = Instant.now().minus(2, ChronoUnit.DAYS);
    EurLexResultDTO lastUpdate = EurLexResultDTO.builder().createdAt(twoDaysBefore).build();
    when(repository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(lastUpdate));
    doNothing().when(service).requestNewestDecisions(anyInt(), any(LocalDate.class));

    service.getSearchResults(
        "0",
        documentationOffice,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    verify(service, times(1))
        .requestNewestDecisions(1, LocalDate.ofInstant(twoDaysBefore, ZoneId.of("Europe/Berlin")));
  }
}
