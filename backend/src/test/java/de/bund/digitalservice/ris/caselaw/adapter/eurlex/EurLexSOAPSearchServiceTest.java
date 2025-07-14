package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({EurLexSOAPSearchService.class})
@TestPropertySource(
    properties = {
      "eurlex.url=https://eur-lex.europa.eu/EURLexWebService?WSDL",
    })
class EurLexSOAPSearchServiceTest {
  @MockitoBean private EurLexResultRepository repository;

  @MockitoBean private DatabaseCourtRepository courtRepository;

  @MockitoBean private HttpEurlexRetrievalService httpEurlexRetrievalService;

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
    verify(repository, never())
        .findAllNewWithUriBySearchParameters(any(), any(), any(), any(), any(), any());
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
    verify(repository, never())
        .findAllNewWithUriBySearchParameters(any(), any(), any(), any(), any(), any());
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
    verify(repository, never())
        .findAllNewWithUriBySearchParameters(any(), any(), any(), any(), any(), any());
  }

  @Test
  void testGetSearchResults_withoutPage_shouldCallRepositoryRequestWithPageZero() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("DS").build();
    when(repository.findAllNewWithUriBySearchParameters(
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
        .findAllNewWithUriBySearchParameters(
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
    when(repository.findAllNewWithUriBySearchParameters(
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
        .findAllNewWithUriBySearchParameters(
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
    when(repository.findAllNewWithUriBySearchParameters(
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
        .findAllNewWithUriBySearchParameters(
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
    when(repository.findAllNewWithUriBySearchParameters(
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
        .findAllNewWithUriBySearchParameters(
            any(Pageable.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.of("EuG")),
            eq(Optional.empty()),
            eq(Optional.empty()));
  }

  @Test
  void updateResultsStatus_withExistingCelexNumbers_shouldUpdate() {
    String celex1 = "celex1";
    String celex2 = "celex2";
    EurLexResultDTO dto1 =
        EurLexResultDTO.builder().status(EurLexResultStatus.NEW).createdAt(Instant.now()).build();
    EurLexResultDTO dto2 =
        EurLexResultDTO.builder().status(EurLexResultStatus.NEW).createdAt(Instant.now()).build();
    when(repository.findAllByCelexNumbers(List.of(celex1, celex2))).thenReturn(List.of(dto1, dto2));
    ArgumentCaptor<List<EurLexResultDTO>> captor = ArgumentCaptor.forClass((Class) List.class);

    service.updateResultStatus(List.of(celex1, celex2));

    verify(repository).saveAll(captor.capture());
    assertThat(captor.getValue().getFirst().getStatus()).isEqualTo(EurLexResultStatus.ASSIGNED);
    assertThat(captor.getValue().getLast().getStatus()).isEqualTo(EurLexResultStatus.ASSIGNED);
  }

  @Test
  void updateResultsStatus_withNonExistentCelexNumbers_shouldDoNothing() {
    String celex1 = "celex1";
    String celex2 = "celex2";

    service.updateResultStatus(List.of(celex1, celex2));

    verify(repository, never()).saveAll(anyList());
  }

  @Test
  void requestNewestDecisions_shouldInsertNew() throws IOException {
    var searchResponse = Files.readString(Paths.get("src/test/resources/testdata/eurlex.xml"));
    when(httpEurlexRetrievalService.requestEurlexResultList(any(), any()))
        .thenReturn(searchResponse);
    ArgumentCaptor<List<EurLexResultDTO>> captor = ArgumentCaptor.forClass((Class) List.class);

    service.requestNewestDecisions();

    verify(repository).saveAll(captor.capture());
    var capturedSearchResults = captor.getValue();
    assertThat(capturedSearchResults.size()).isEqualTo(3);
    assertThat(capturedSearchResults.get(0).getCelex()).isEqualTo("62024CO0878");
    assertThat(capturedSearchResults.get(0).getStatus()).isEqualTo(EurLexResultStatus.NEW);
    assertThat(capturedSearchResults.get(1).getCelex()).isEqualTo("62023CJ0538");
    assertThat(capturedSearchResults.get(1).getStatus()).isEqualTo(EurLexResultStatus.NEW);
    assertThat(capturedSearchResults.get(2).getCelex()).isEqualTo("62019CV0001(02)");
    assertThat(capturedSearchResults.get(2).getStatus()).isEqualTo(EurLexResultStatus.NEW);
  }

  @Test
  void requestNewestDecisions_shouldUpdateExistingAndInsertNew() throws IOException {
    var searchResponse = Files.readString(Paths.get("src/test/resources/testdata/eurlex.xml"));
    when(httpEurlexRetrievalService.requestEurlexResultList(any(), any()))
        .thenReturn(searchResponse);
    ArgumentCaptor<List<EurLexResultDTO>> captor = ArgumentCaptor.forClass((Class) List.class);
    var uuid = UUID.randomUUID();
    when(repository.findByCelexNumber("62024CO0878"))
        .thenReturn(
            Optional.of(
                EurLexResultDTO.builder()
                    .celex("62024CO0878")
                    .status(EurLexResultStatus.ASSIGNED)
                    .id(uuid)
                    .build()));

    service.requestNewestDecisions();

    verify(repository).saveAll(captor.capture());
    var capturedSearchResults = captor.getValue();
    assertThat(capturedSearchResults.size()).isEqualTo(3);
    assertThat(capturedSearchResults.get(0).getCelex()).isEqualTo("62024CO0878");
    assertThat(capturedSearchResults.get(0).getStatus()).isEqualTo(EurLexResultStatus.ASSIGNED);
    assertThat(capturedSearchResults.get(0).getId()).isEqualTo(uuid);
    assertThat(capturedSearchResults.get(1).getCelex()).isEqualTo("62023CJ0538");
    assertThat(capturedSearchResults.get(1).getStatus()).isEqualTo(EurLexResultStatus.NEW);
    assertThat(capturedSearchResults.get(2).getCelex()).isEqualTo("62019CV0001(02)");
    assertThat(capturedSearchResults.get(2).getStatus()).isEqualTo(EurLexResultStatus.NEW);
  }
}
