package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({FieldOfLawService.class})
class FieldOfLawServiceTest {
  @Autowired FieldOfLawService service;

  @MockBean FieldOfLawRepository repository;

  @Test
  void testGetFieldsOfLaw_withoutQuery_shouldntCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findAllByOrderByIdentifierAsc(pageable)).thenReturn(Flux.empty());
    when(repository.count()).thenReturn(Mono.just(0L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.empty(), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findAllByOrderByIdentifierAsc(pageable);
    verify(repository, times(1)).count();
  }

  @Test
  void testGetFieldsOfLaw_withEmptyQuery_shouldntCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findAllByOrderByIdentifierAsc(pageable)).thenReturn(Flux.empty());
    when(repository.count()).thenReturn(Mono.just(0L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(""), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findAllByOrderByIdentifierAsc(pageable);
    verify(repository, times(1)).count();
  }

  @Test
  void testGetFieldsOfLaw_withQuery_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    String[] searchTerms = new String[] {"test"};
    when(repository.findBySearchTerms(searchTerms)).thenReturn(Flux.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of("test"), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findBySearchTerms(searchTerms);
    verify(repository, never()).findAllByOrderByIdentifierAsc(pageable);
  }

  @Test
  void
      testGetFieldsOfLaw_withQueryWithWhitespaceAtTheStartAndTheEnd_shouldCallRepositoryWithTrimmedSearchString() {
    Pageable pageable = PageRequest.of(0, 10);
    String[] searchTerms = new String[] {"test"};
    when(repository.findBySearchTerms(searchTerms)).thenReturn(Flux.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(" test  \t"), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findBySearchTerms(searchTerms);
    verify(repository, never()).findAllByOrderByIdentifierAsc(pageable);
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsEmpty_shouldCallRepository() {
    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("")).thenReturn(Flux.empty());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("")).verifyComplete();

    verify(repository, times(1)).findAllByParentIdentifierOrderByIdentifierAsc("");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsRoot_shouldCallRepository() {
    when(repository.getTopLevelNodes()).thenReturn(Flux.empty());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("root")).verifyComplete();

    verify(repository, times(1)).getTopLevelNodes();
    verify(repository, never()).findAllByParentIdentifierOrderByIdentifierAsc(anyString());
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumber_shouldCallRepository() {
    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("test")).thenReturn(Flux.empty());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findAllByParentIdentifierOrderByIdentifierAsc("test");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberDoesntExist() {
    when(repository.findByIdentifier("test")).thenReturn(Mono.empty());

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findByIdentifier("test");
    verify(repository, never()).findParentByChild(any(FieldOfLaw.class));
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtTopLevel() {
    FieldOfLaw child = FieldOfLaw.builder().identifier("test").build();
    when(repository.findByIdentifier("test")).thenReturn(Mono.just(child));
    when(repository.findParentByChild(child)).thenReturn(Mono.empty());

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findByIdentifier("test");
    verify(repository, times(1)).findParentByChild(child);
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtSecondLevel() {
    FieldOfLaw child = FieldOfLaw.builder().identifier("child").build();
    when(repository.findByIdentifier("child")).thenReturn(Mono.just(child));
    FieldOfLaw parent =
        FieldOfLaw.builder().identifier("parent").children(new ArrayList<>()).build();
    when(repository.findParentByChild(child)).thenReturn(Mono.just(parent));
    when(repository.findParentByChild(parent)).thenReturn(Mono.just(parent));

    StepVerifier.create(service.getTreeForFieldOfLaw("child"))
        .consumeNextWith(
            result -> {
              assertThat(result.getIdentifier()).isEqualTo("parent");
              assertThat(result.getChildren()).extracting("identifier").containsExactly("child");
            })
        .verifyComplete();

    verify(repository, times(1)).findByIdentifier("child");
    verify(repository, times(1)).findParentByChild(child);
  }

  @Test
  void testGetFieldsOfLaw_withSearchString() {
    String searchString = "stext";
    String[] searchTerms = new String[] {searchString};
    FieldOfLaw expectedFieldOfLaw =
        new FieldOfLaw(
            2L,
            0,
            "TS-01-01",
            "stext 2",
            Collections.emptyList(),
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")),
            new ArrayList<>(),
            null);

    Pageable pageable = PageRequest.of(0, 10);
    PageImpl<FieldOfLaw> page = new PageImpl<>(List.of(expectedFieldOfLaw), pageable, 1);

    when(repository.findBySearchTerms(searchTerms)).thenReturn(Flux.just(expectedFieldOfLaw));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(searchString), pageable))
        .consumeNextWith(fieldOfLawPage -> assertThat(fieldOfLawPage).isEqualTo(page))
        .verifyComplete();

    verify(repository).findBySearchTerms(searchTerms);
  }

  @Test
  void testGetFieldOfLawChildren() {
    FieldOfLaw expectedFieldOfLaw =
        new FieldOfLaw(
            2L,
            1,
            "TS-01-01",
            "stext 2",
            Collections.emptyList(),
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")),
            new ArrayList<>(),
            null);

    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("TS-01-01"))
        .thenReturn(Flux.just(expectedFieldOfLaw));

    StepVerifier.create(service.getChildrenOfFieldOfLaw("TS-01-01"))
        .consumeNextWith(fieldOfLaw -> assertThat(fieldOfLaw).isEqualTo(expectedFieldOfLaw))
        .verifyComplete();

    verify(repository).findAllByParentIdentifierOrderByIdentifierAsc("TS-01-01");
  }
}
