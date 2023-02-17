package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import java.util.ArrayList;
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
    verify(repository, never()).findParentByChild(any(SubjectField.class));
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtTopLevel() {
    SubjectField child = SubjectField.builder().subjectFieldNumber("test").build();
    when(repository.findBySubjectFieldNumber("test")).thenReturn(Mono.just(child));
    when(repository.findParentByChild(child)).thenReturn(Mono.empty());

    StepVerifier.create(service.getTreeForFieldOfLaw("test")).verifyComplete();

    verify(repository, times(1)).findBySubjectFieldNumber("test");
    verify(repository, times(1)).findParentByChild(child);
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtSecondLevel() {
    SubjectField child = SubjectField.builder().subjectFieldNumber("child").build();
    when(repository.findBySubjectFieldNumber("child")).thenReturn(Mono.just(child));
    SubjectField parent =
        SubjectField.builder().subjectFieldNumber("parent").children(new ArrayList<>()).build();
    when(repository.findParentByChild(child)).thenReturn(Mono.just(parent));
    when(repository.findParentByChild(parent)).thenReturn(Mono.just(parent));

    StepVerifier.create(service.getTreeForFieldOfLaw("child"))
        .consumeNextWith(
            result -> {
              assertThat(result.subjectFieldNumber()).isEqualTo("parent");
              assertThat(result.children())
                  .extracting("subjectFieldNumber")
                  .containsExactly("child");
            })
        .verifyComplete();

    verify(repository, times(1)).findBySubjectFieldNumber("child");
    verify(repository, times(1)).findParentByChild(child);
  }
}
