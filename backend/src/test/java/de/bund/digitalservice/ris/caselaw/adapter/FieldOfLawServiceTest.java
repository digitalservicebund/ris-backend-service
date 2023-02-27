package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    when(repository.findAllByOrderBySubjectFieldNumberAsc()).thenReturn(Flux.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.empty())).verifyComplete();

    verify(repository, times(1)).findAllByOrderBySubjectFieldNumberAsc();
    verify(repository, never()).findBySearchStr(anyString());
  }

  @Test
  void testGetFieldsOfLaw_withEmptyQuery_shouldntCallRepository() {
    when(repository.findAllByOrderBySubjectFieldNumberAsc()).thenReturn(Flux.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(""))).verifyComplete();

    verify(repository, times(1)).findAllByOrderBySubjectFieldNumberAsc();
    verify(repository, never()).findBySearchStr(anyString());
  }

  @Test
  void testGetFieldsOfLaw_withQuery_shouldCallRepository() {
    when(repository.findBySearchStr("test")).thenReturn(Flux.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of("test"))).verifyComplete();

    verify(repository, times(1)).findBySearchStr("test");
    verify(repository, never()).findAllByOrderBySubjectFieldNumberAsc();
  }

  @Test
  void
      testGetFieldsOfLaw_withQueryWithWhitespaceAtTheStartAndTheEnd_shouldCallRepositoryWithTrimmedSearchString() {
    when(repository.findBySearchStr("test")).thenReturn(Flux.empty());

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(" test  \t")))
        .verifyComplete();

    verify(repository, times(1)).findBySearchStr("test");
    verify(repository, never()).findAllByOrderBySubjectFieldNumberAsc();
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

    when(repository.findBySearchStr(searchString)).thenReturn(Flux.just(expectedFieldOfLaw));

    StepVerifier.create(service.getFieldsOfLawBySearchQuery(Optional.of(searchString)))
        .consumeNextWith(subjectField -> assertThat(subjectField).isEqualTo(expectedFieldOfLaw))
        .verifyComplete();

    verify(repository).findBySearchStr(searchString);
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
