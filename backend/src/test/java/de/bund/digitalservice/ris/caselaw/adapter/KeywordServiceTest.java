package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Norm;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({FieldOfLawService.class})
class FieldOfLawServiceTest {
  @Autowired FieldOfLawService service;

  @MockBean SubjectFieldRepository repository;

  @Test
  void testGetFieldsOfLaw_withoutQuery_shouldntCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findAllByOrderBySubjectFieldNumberAsc(pageable)).thenReturn(Flux.empty());
    when(repository.count()).thenReturn(Mono.just(0L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.empty(), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findAllByOrderBySubjectFieldNumberAsc(pageable);
    verify(repository, times(1)).count();
    verify(repository, never()).findBySearchStr(anyString(), eq(pageable));
  }

  @Test
  void testGetFieldsOfLaw_withEmptyQuery_shouldntCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findAllByOrderBySubjectFieldNumberAsc(pageable)).thenReturn(Flux.empty());
    when(repository.count()).thenReturn(Mono.just(0L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(""), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findAllByOrderBySubjectFieldNumberAsc(pageable);
    verify(repository, times(1)).count();
    verify(repository, never()).findBySearchStr(anyString(), eq(pageable));
  }

  @Test
  void testGetFieldsOfLaw_withQuery_shouldCallRepository() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findBySearchStr("test", pageable)).thenReturn(Flux.empty());
    when(repository.countBySearchStr("test")).thenReturn(Mono.just(0L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of("test"), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findBySearchStr("test", pageable);
    verify(repository, never()).findAllByOrderBySubjectFieldNumberAsc(pageable);
  }

  @Test
  void
      testGetFieldsOfLaw_withQueryWithWhitespaceAtTheStartAndTheEnd_shouldCallRepositoryWithTrimmedSearchString() {
    Pageable pageable = Pageable.unpaged();
    when(repository.findBySearchStr("test", pageable)).thenReturn(Flux.empty());
    when(repository.countBySearchStr("test")).thenReturn(Mono.just(0L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(" test  \t"), pageable))
        .consumeNextWith(
            page -> {
              assertThat(page.getContent()).isEmpty();
              assertThat(page.getTotalElements()).isZero();
            })
        .verifyComplete();

    verify(repository, times(1)).findBySearchStr("test", pageable);
    verify(repository, never()).findAllByOrderBySubjectFieldNumberAsc(pageable);
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsEmpty_shouldCallRepository() {
    when(repository.findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(""))
        .thenReturn(Flux.empty());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("")).verifyComplete();

    verify(repository, times(1)).findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc("");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsRoot_shouldCallRepository() {
    when(repository.getTopLevelNodes()).thenReturn(Flux.empty());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("root")).verifyComplete();

    verify(repository, times(1)).getTopLevelNodes();
    verify(repository, never())
        .findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(anyString());
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumber_shouldCallRepository() {
    when(repository.findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc("test"))
        .thenReturn(Flux.empty());

    StepVerifier.create(service.getChildrenOfFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1))
        .findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc("test");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberDoesntExist() {
    when(repository.findBySubjectFieldNumber("test")).thenReturn(Mono.empty());

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findBySubjectFieldNumber("test");
    verify(repository, never()).findParentByChild(any(FieldOfLaw.class));
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtTopLevel() {
    FieldOfLaw child = FieldOfLaw.builder().identifier("test").build();
    when(repository.findBySubjectFieldNumber("test")).thenReturn(Mono.just(child));
    when(repository.findParentByChild(child)).thenReturn(Mono.empty());

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findBySubjectFieldNumber("test");
    verify(repository, times(1)).findParentByChild(child);
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtSecondLevel() {
    FieldOfLaw child = FieldOfLaw.builder().identifier("child").build();
    when(repository.findBySubjectFieldNumber("child")).thenReturn(Mono.just(child));
    FieldOfLaw parent =
        FieldOfLaw.builder().identifier("parent").children(new ArrayList<>()).build();
    when(repository.findParentByChild(child)).thenReturn(Mono.just(parent));
    when(repository.findParentByChild(parent)).thenReturn(Mono.just(parent));

    StepVerifier.create(service.getTreeForFieldOfLaw("child"))
        .consumeNextWith(
            result -> {
              assertThat(result.identifier()).isEqualTo("parent");
              assertThat(result.children()).extracting("identifier").containsExactly("child");
            })
        .verifyComplete();

    verify(repository, times(1)).findBySubjectFieldNumber("child");
    verify(repository, times(1)).findParentByChild(child);
  }

  @Test
  void testGetSubjectFields_withSearchString() {
    String searchString = "stext";
    FieldOfLaw expectedFieldOfLaw =
        new FieldOfLaw(
            2L,
            0,
            "TS-01-01",
            "stext 2",
            Collections.emptyList(),
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")),
            new ArrayList<>());

    Pageable pageable = Pageable.unpaged();
    PageImpl<FieldOfLaw> page = new PageImpl<>(List.of(expectedFieldOfLaw), pageable, 1);

    when(repository.findBySearchStr(searchString, pageable))
        .thenReturn(Flux.just(expectedFieldOfLaw));
    when(repository.countBySearchStr(searchString)).thenReturn(Mono.just(1L));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(searchString), pageable))
        .consumeNextWith(subjectField -> assertThat(subjectField).isEqualTo(page))
        .verifyComplete();

    verify(repository).findBySearchStr(searchString, pageable);
    verify(repository).countBySearchStr(searchString);
  }

  @Test
  void testGetSubjectFieldChildren() {
    FieldOfLaw expectedFieldOfLaw =
        new FieldOfLaw(
            2L,
            1,
            "TS-01-01",
            "stext 2",
            Collections.emptyList(),
            List.of(new Keyword("keyword")),
            List.of(new Norm("abbr1", "description")),
            new ArrayList<>());

    when(repository.findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc("TS-01-01"))
        .thenReturn(Flux.just(expectedFieldOfLaw));

    StepVerifier.create(service.getChildrenOfFieldOfLaw("TS-01-01"))
        .consumeNextWith(subjectField -> assertThat(subjectField).isEqualTo(expectedFieldOfLaw))
        .verifyComplete();

    verify(repository).findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc("TS-01-01");
  }
}
