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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({FieldOfLawService.class})
class FieldOfLawServiceTest {
  @Autowired FieldOfLawService service;

  @MockBean FieldOfLawRepository repository;

  //  @Test
  //  void testGetFieldsOfLaw_withoutQuery_shouldNotCallRepository() {
  //    Pageable pageable = Pageable.unpaged();
  //
  //    var page =
  //        service.getFieldsOfLawBySearchQuery(
  //            Optional.empty(), Optional.empty(), Optional.empty(), pageable);
  //    assertThat(page.getContent()).isEmpty();
  //    assertThat(page.isEmpty()).isTrue();
  //
  //    verify(repository, never()).findByIdentifier(any(), any());
  //    verify(repository, never()).findByNorm(any());
  //    verify(repository, never()).findBySearchTerms(any());
  //    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
  //    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
  //    verify(repository, never()).findByIdentifierAndNorm(any(), any());
  //    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  //  }

  // TODO figure out what the behaviour for empty query should be

  //  @Test
  //  void testGetFieldsOfLaw_withEmptyQuery_shouldNotCallRepository() {
  //    Pageable pageable = Pageable.unpaged();
  //
  //    var page =
  //        service.getFieldsOfLawBySearchQuery(
  //            Optional.of(""), Optional.of(""), Optional.of(""), pageable);
  //    assertThat(page.getContent()).isEmpty();
  //    assertThat(page.isEmpty()).isTrue();
  //
  //    verify(repository, never()).findByIdentifier(any(), any());
  //    verify(repository, never()).findBySearchTerms(any());
  //    verify(repository, never()).findByNorm(any());
  //    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
  //    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
  //    verify(repository, never()).findByIdentifierAndNorm(any(), any());
  //    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  //  }

  @Test
  void testGetFieldsOfLaw_withIdentifier_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var identifierString = "foo";
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findByIdentifier(identifierString, pageable)).thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.of(identifierString), Optional.empty(), Optional.empty(), pageable);
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);
    assertThat(page.isEmpty()).isFalse();

    verify(repository).findByIdentifier(identifierString, pageable);
    verify(repository, never()).findBySearchTerms(any());
    verify(repository, never()).findByNorm(any());
    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
    verify(repository, never()).findByIdentifierAndNorm(any(), any());
    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  }

  @Test
  void testGetFieldsOfLaw_withSearchTerm_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var searchTerms = new String[] {"foo", "bar"};
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findBySearchTerms(searchTerms)).thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.empty(), Optional.of("foo bar"), Optional.empty(), pageable);
    assertThat(page.isEmpty()).isFalse();
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);

    verify(repository, never()).findByIdentifier(any(), any());
    verify(repository).findBySearchTerms(searchTerms);
    verify(repository, never()).findByNorm(any());
    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
    verify(repository, never()).findByIdentifierAndNorm(any(), any());
    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  }

  @Test
  void testGetFieldsOfLaw_withNorm_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var normString = "foo";
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findByNorm(normString)).thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.empty(), Optional.empty(), Optional.of(normString), pageable);
    assertThat(page.isEmpty()).isFalse();
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);

    verify(repository, never()).findByIdentifier(any(), any());
    verify(repository, never()).findBySearchTerms(any());
    verify(repository).findByNorm(normString);
    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
    verify(repository, never()).findByIdentifierAndNorm(any(), any());
    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  }

  @Test
  void testGetFieldsOfLaw_withIdentifierAndSearchTermAndNorm_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var identifierString = "foo";
    var searchTerms = new String[] {"foo", "bar"};
    var normString = "bar";
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findByIdentifierAndSearchTermsAndNorm(
            identifierString, searchTerms, normString))
        .thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.of(identifierString),
            Optional.of("foo bar"),
            Optional.of(normString),
            pageable);
    assertThat(page.isEmpty()).isFalse();
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);

    verify(repository, never()).findByIdentifier(any(), any());
    verify(repository, never()).findBySearchTerms(any());
    verify(repository, never()).findByNorm(any());
    verify(repository)
        .findByIdentifierAndSearchTermsAndNorm(identifierString, searchTerms, normString);
    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
    verify(repository, never()).findByIdentifierAndNorm(any(), any());
    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  }

  @Test
  void testGetFieldsOfLaw_withIdentifierAndSearchTerm_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var identifierString = "foo";
    var searchTerms = new String[] {"foo", "bar"};
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findByIdentifierAndSearchTerms(identifierString, searchTerms))
        .thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.of(identifierString), Optional.of("foo bar"), Optional.empty(), pageable);
    assertThat(page.isEmpty()).isFalse();
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);

    verify(repository, never()).findByIdentifier(any(), any());
    verify(repository, never()).findBySearchTerms(any());
    verify(repository, never()).findByNorm(any());
    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
    verify(repository).findByIdentifierAndSearchTerms(identifierString, searchTerms);
    verify(repository, never()).findByIdentifierAndNorm(any(), any());
    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  }

  @Test
  void testGetFieldsOfLaw_withIdentifierAndNorm_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var identifierString = "foo";
    var normString = "bar";
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findByIdentifierAndNorm(identifierString, normString))
        .thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.of(identifierString), Optional.empty(), Optional.of(normString), pageable);
    assertThat(page.isEmpty()).isFalse();
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);

    verify(repository, never()).findByIdentifier(any(), any());
    verify(repository, never()).findBySearchTerms(any());
    verify(repository, never()).findByNorm(any());
    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
    verify(repository).findByIdentifierAndNorm(identifierString, normString);
    verify(repository, never()).findByNormAndSearchTerms(any(), any());
  }

  @Test
  void testGetFieldsOfLaw_withSearchTermAndNorm_shouldCallRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    var searchTerms = new String[] {"foo", "bar"};
    var normString = "bar";
    var expectedFieldsOfLaw = List.of(generateFieldOfLaw());
    when(repository.findByNormAndSearchTerms(normString, searchTerms))
        .thenReturn(expectedFieldsOfLaw);

    var page =
        service.getFieldsOfLawBySearchQuery(
            Optional.empty(), Optional.of("foo bar"), Optional.of(normString), pageable);
    assertThat(page.isEmpty()).isFalse();
    assertThat(page.getContent()).isEqualTo(expectedFieldsOfLaw);

    verify(repository, never()).findByIdentifier(any(), any());
    verify(repository, never()).findBySearchTerms(any());
    verify(repository, never()).findByNorm(any());
    verify(repository, never()).findByIdentifierAndSearchTermsAndNorm(any(), any(), any());
    verify(repository, never()).findByIdentifierAndSearchTerms(any(), any());
    verify(repository, never()).findByIdentifierAndNorm(any(), any());
    verify(repository).findByNormAndSearchTerms(normString, searchTerms);
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsEmpty_shouldCallRepository() {
    when(repository.findAllByParentIdentifierOrderByIdentifierAsc(""))
        .thenReturn(Collections.emptyList());

    Assertions.assertTrue(service.getChildrenOfFieldOfLaw("").isEmpty());

    verify(repository, times(1)).findAllByParentIdentifierOrderByIdentifierAsc("");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumberIsRoot_shouldCallRepository() {
    when(repository.getTopLevelNodes()).thenReturn(Collections.emptyList());

    Assertions.assertTrue(service.getChildrenOfFieldOfLaw("root").isEmpty());

    verify(repository, times(1)).getTopLevelNodes();
    verify(repository, never()).findAllByParentIdentifierOrderByIdentifierAsc(anyString());
  }

  @Test
  void testGetChildrenOfFieldOfLaw_withNumber_shouldCallRepository() {
    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("test"))
        .thenReturn(Collections.emptyList());

    Assertions.assertTrue(service.getChildrenOfFieldOfLaw("test").isEmpty());

    verify(repository, times(1)).findAllByParentIdentifierOrderByIdentifierAsc("test");
    verify(repository, never()).getTopLevelNodes();
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberDoesntExist() {
    when(repository.findTreeByIdentifier("test")).thenReturn(null);
    service.getTreeForFieldOfLaw("test");
    verify(repository, times(1)).findTreeByIdentifier("test");
  }

  @Test
  void testGetTreeForFieldOfLaw_withFieldNumberAtTopLevel() {
    FieldOfLaw child = FieldOfLaw.builder().identifier("test").build();
    when(repository.findTreeByIdentifier("test")).thenReturn(child);

    var folTree = service.getTreeForFieldOfLaw("test");
    Assertions.assertNotNull(folTree);

    verify(repository, times(1)).findTreeByIdentifier("test");
  }

  @Test
  void testGetFieldOfLawChildren() {
    FieldOfLaw expectedFieldOfLaw =
        FieldOfLaw.builder()
            .id(UUID.randomUUID())
            .hasChildren(true)
            .identifier("TS-01-01")
            .text("stext 2")
            .linkedFields(Collections.emptyList())
            .norms(List.of(new Norm("abbr1", "description")))
            .children(new ArrayList<>())
            .build();

    when(repository.findAllByParentIdentifierOrderByIdentifierAsc("TS-01-01"))
        .thenReturn(List.of(expectedFieldOfLaw));

    var response = service.getChildrenOfFieldOfLaw("TS-01-01");

    assertThat(response).hasSize(1);
    assertThat(response).extracting("identifier").containsExactly("TS-01" + "-01");

    verify(repository).findAllByParentIdentifierOrderByIdentifierAsc("TS-01-01");
  }

  FieldOfLaw generateFieldOfLaw() {
    return FieldOfLaw.builder()
        .id(UUID.randomUUID())
        .hasChildren(false)
        .identifier("TS-01-01")
        .text("stext 2")
        .linkedFields(Collections.emptyList())
        .norms(List.of(new Norm("abbr1", "description")))
        .children(Collections.emptyList())
        .build();
  }
}
