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
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({FieldOfLawService.class})
class FieldOfLawServiceTest {
  @Autowired FieldOfLawService service;

  @MockBean FieldOfLawRepository repository;

  @Test
  void testGetFieldsOfLaw_withoutQuery_shouldntCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findAllByOrderByIdentifierAsc(pageable)).thenReturn(Page.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.empty(), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findAllByOrderByIdentifierAsc(pageable);
  }

  @Test
  void testGetFieldsOfLaw_withEmptyQuery_shouldntCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findAllByOrderByIdentifierAsc(pageable)).thenReturn(Page.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(""), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findAllByOrderByIdentifierAsc(pageable);
  }

  @Test
  void testGetFieldsOfLaw_withQuery_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    String[] searchTerms = new String[] {"test"};
    when(repository.findBySearchTerms(searchTerms)).thenReturn(Collections.emptyList());

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
    when(repository.findBySearchTerms(searchTerms)).thenReturn(Collections.emptyList());

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
    when(repository.findAllByParentIdentifierOrderByIdentifierAsc(""))
        .thenReturn(Collections.emptyList());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("")).expectNext(Collections.emptyList());

    verify(repository, times(1)).findAllByParentIdentifierOrderByIdentifierAsc("");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsRoot_shouldCallRepository() {
    when(repository.getTopLevelNodes()).thenReturn(Collections.emptyList());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("root"))
        .expectNext(Collections.emptyList());

    verify(repository, times(1)).getTopLevelNodes();
    verify(repository, never()).findAllByParentIdentifierOrderByIdentifierAsc(anyString());
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumber_shouldCallRepository() {
    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("test"))
        .thenReturn(Collections.emptyList());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("test"))
        .expectNext(Collections.emptyList());

    verify(repository, times(1)).findAllByParentIdentifierOrderByIdentifierAsc("test");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberDoesntExist() {
    when(repository.findTreeByIdentifier("test")).thenReturn(null);

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findTreeByIdentifier("test");
    verify(repository, never()).findParentByChild(any(FieldOfLaw.class));
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtTopLevel() {
    FieldOfLaw child = FieldOfLaw.builder().identifier("test").build();
    when(repository.findTreeByIdentifier("test")).thenReturn(child);
    when(repository.findParentByChild(child)).thenReturn(null);

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).expectNext(child).verifyComplete();

    verify(repository, times(1)).findTreeByIdentifier("test");
  }

  @Test
  void testGetFieldsOfLaw_withSearchString() {
    String searchString = "stext";
    String[] searchTerms = new String[] {searchString};
    FieldOfLaw expectedFieldOfLaw =
        FieldOfLaw.builder()
            .id(UUID.randomUUID())
            .childrenCount(0)
            .identifier("TS-01-01")
            .text("stext 2")
            .linkedFields(Collections.emptyList())
            .norms(List.of(new Norm("abbr1", "description")))
            .children(Collections.emptyList())
            .build();

    Pageable pageable = PageRequest.of(0, 10);
    PageImpl<FieldOfLaw> page = new PageImpl<>(List.of(expectedFieldOfLaw), pageable, 1);

    when(repository.findBySearchTerms(searchTerms)).thenReturn(List.of(expectedFieldOfLaw));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(searchString), pageable))
        .consumeNextWith(fieldOfLawPage -> assertThat(fieldOfLawPage).isEqualTo(page))
        .verifyComplete();

    verify(repository).findBySearchTerms(searchTerms);
  }

  @Test
  void testGetFieldOfLawChildren() {
    FieldOfLaw expectedFieldOfLaw =
        FieldOfLaw.builder()
            .id(UUID.randomUUID())
            .childrenCount(1)
            .identifier("TS-01-01")
            .text("stext 2")
            .linkedFields(Collections.emptyList())
            .norms(List.of(new Norm("abbr1", "description")))
            .children(new ArrayList<>())
            .build();

    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("TS-01-01"))
        .thenReturn(List.of(expectedFieldOfLaw));

    StepVerifier.create(service.getChildrenOfFieldOfLaw("TS-01-01"))
        .consumeNextWith(
            response -> {
              assertThat(response).hasSize(1);
              assertThat(response).extracting("identifier").containsExactly("TS-01-01");
            })
        .verifyComplete();

    verify(repository).findAllByParentIdentifierOrderByIdentifierAsc("TS-01-01");
  }

  @Test
  void testSearchAndOrderByScore_pageableOffsetGreaterThanResultListSize() {
    FieldOfLaw databaseFieldOfLaw = FieldOfLaw.builder().build();

    when(repository.findBySearchTerms(any(String[].class))).thenReturn(List.of(databaseFieldOfLaw));

    Page<FieldOfLaw> result = service.searchAndOrderByScore("foo", PageRequest.of(1, 5));
    assertThat(result).isEmpty();
  }
}
