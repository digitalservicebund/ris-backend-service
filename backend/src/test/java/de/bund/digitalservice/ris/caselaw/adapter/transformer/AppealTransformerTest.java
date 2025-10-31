package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealAppellantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealJointRevisionDefendantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealJointRevisionPlaintiffDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealNzbDefendantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealNzbPlaintiffDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealRevisionDefendantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealRevisionPlaintiffDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppellantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appeal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealWithdrawal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import de.bund.digitalservice.ris.caselaw.domain.appeal.PkhPlaintiff;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppealTransformerTest {

  @Test
  void transformToDTO_withNull_shouldReturnNull() {
    AppealDTO appealDTO =
        AppealTransformer.transformToDTO(generateDecisionDTOBuilder().build(), null);

    assertThat(appealDTO).isNull();
  }

  @Test
  void transformToDTO_withEmptyObject_shouldReturnNull() {
    Appeal appeal = Appeal.builder().build();

    AppealDTO appealDTO =
        AppealTransformer.transformToDTO(generateDecisionDTOBuilder().build(), appeal);

    assertThat(appealDTO).isNull();
  }

  @Test
  void transformToDTO_withEmptyLists_shouldReturnNull() {
    Appeal appeal =
        Appeal.builder()
            .appellants(Collections.emptyList())
            .revisionDefendantStatuses(Collections.emptyList())
            .revisionPlaintiffStatuses(Collections.emptyList())
            .jointRevisionDefendantStatuses(Collections.emptyList())
            .jointRevisionPlaintiffStatuses(Collections.emptyList())
            .nzbDefendantStatuses(Collections.emptyList())
            .nzbPlaintiffStatuses(Collections.emptyList())
            .build();

    AppealDTO appealDTO =
        AppealTransformer.transformToDTO(generateDecisionDTOBuilder().build(), appeal);

    assertThat(appealDTO).isNull();
  }

  @Test
  void transformToDTO_withExistingAppeal_shouldUpdateExisting() {
    Appeal appeal = Appeal.builder().appealWithdrawal(AppealWithdrawal.JA).build();
    var existingId = UUID.randomUUID();
    AppealDTO existing = AppealDTO.builder().id(existingId).build();
    var currentDecision = generateDecisionDTOBuilder().appeal(existing).build();

    AppealDTO appealDTO = AppealTransformer.transformToDTO(currentDecision, appeal);

    assertThat(appealDTO.getId()).isEqualTo(existingId);
    assertThat(appealDTO.getAppealWithdrawal()).isEqualTo(AppealWithdrawal.JA);
  }

  @Test
  void transformToDTO_shouldSetAllFields() {
    Appeal appeal =
        Appeal.builder()
            .appellants(List.of(Appellant.builder().value("Kläger").build()))
            .revisionDefendantStatuses(List.of(AppealStatus.builder().value("unbegründet").build()))
            .revisionPlaintiffStatuses(List.of(AppealStatus.builder().value("unbegründet").build()))
            .jointRevisionDefendantStatuses(
                List.of(AppealStatus.builder().value("unbegründet").build()))
            .jointRevisionPlaintiffStatuses(
                List.of(AppealStatus.builder().value("unbegründet").build()))
            .nzbDefendantStatuses(List.of(AppealStatus.builder().value("unbegründet").build()))
            .nzbPlaintiffStatuses(List.of(AppealStatus.builder().value("unbegründet").build()))
            .appealWithdrawal(AppealWithdrawal.JA)
            .pkhPlaintiff(PkhPlaintiff.JA)
            .build();

    AppealDTO appealDTO =
        AppealTransformer.transformToDTO(generateDecisionDTOBuilder().build(), appeal);

    assertThat(appealDTO).isNotNull();
    assertThat(appealDTO.getAppellants().getFirst().getAppellant().getValue()).isEqualTo("Kläger");
    assertThat(appealDTO.getRevisionDefendantStatuses().getFirst().getAppealStatus().getValue())
        .isEqualTo("unbegründet");
    assertThat(appealDTO.getRevisionPlaintiffStatuses().getFirst().getAppealStatus().getValue())
        .isEqualTo("unbegründet");
    assertThat(
            appealDTO.getJointRevisionDefendantStatuses().getFirst().getAppealStatus().getValue())
        .isEqualTo("unbegründet");
    assertThat(
            appealDTO.getJointRevisionPlaintiffStatuses().getFirst().getAppealStatus().getValue())
        .isEqualTo("unbegründet");
    assertThat(appealDTO.getNzbDefendantStatuses().getFirst().getAppealStatus().getValue())
        .isEqualTo("unbegründet");
    assertThat(appealDTO.getNzbPlaintiffStatuses().getFirst().getAppealStatus().getValue())
        .isEqualTo("unbegründet");
    assertThat(appealDTO.getAppealWithdrawal()).isEqualTo(AppealWithdrawal.JA);
    assertThat(appealDTO.getPkhPlaintiff()).isEqualTo(PkhPlaintiff.JA);
  }

  @Test
  void transformToDTO_withEmptyLists_shouldSetListsToNull() {
    Appeal appeal =
        Appeal.builder()
            .appellants(Collections.emptyList())
            .revisionDefendantStatuses(Collections.emptyList())
            .revisionPlaintiffStatuses(Collections.emptyList())
            .jointRevisionDefendantStatuses(Collections.emptyList())
            .jointRevisionPlaintiffStatuses(Collections.emptyList())
            .nzbDefendantStatuses(Collections.emptyList())
            .nzbPlaintiffStatuses(Collections.emptyList())
            .appealWithdrawal(AppealWithdrawal.JA)
            .pkhPlaintiff(PkhPlaintiff.JA)
            .build();

    AppealDTO appealDTO =
        AppealTransformer.transformToDTO(generateDecisionDTOBuilder().build(), appeal);

    assertThat(appealDTO).isNotNull();
    assertThat(appealDTO.getAppellants()).isNull();
    assertThat(appealDTO.getRevisionDefendantStatuses()).isNull();
    assertThat(appealDTO.getRevisionPlaintiffStatuses()).isNull();
    assertThat(appealDTO.getJointRevisionDefendantStatuses()).isNull();
    assertThat(appealDTO.getJointRevisionPlaintiffStatuses()).isNull();
    assertThat(appealDTO.getNzbDefendantStatuses()).isNull();
    assertThat(appealDTO.getNzbPlaintiffStatuses()).isNull();
    assertThat(appealDTO.getAppealWithdrawal()).isEqualTo(AppealWithdrawal.JA);
    assertThat(appealDTO.getPkhPlaintiff()).isEqualTo(PkhPlaintiff.JA);
  }

  @Test
  void transformToDomain_withNull_shouldReturnNull() {
    Appeal appeal = AppealTransformer.transformToDomain(null);

    assertThat(appeal).isNull();
  }

  @Test
  void transformToDomain_shouldSetAllFields() {
    var id = UUID.randomUUID();
    AppealDTO appealDTO =
        AppealDTO.builder()
            .id(id)
            .appealWithdrawal(AppealWithdrawal.JA)
            .pkhPlaintiff(PkhPlaintiff.JA)
            .build();
    var appellants =
        List.of(
            AppealAppellantDTO.builder()
                .appeal(appealDTO)
                .appellant(AppellantDTO.builder().value("Kläger").build())
                .build());
    var revisionDefendants =
        List.of(
            AppealRevisionDefendantDTO.builder()
                .appeal(appealDTO)
                .appealStatus(AppealStatusDTO.builder().value("unzulässig").build())
                .build());
    var revisionPlaintiffs =
        List.of(
            AppealRevisionPlaintiffDTO.builder()
                .appeal(appealDTO)
                .appealStatus(AppealStatusDTO.builder().value("begründet").build())
                .build());
    var jointRevisionDefendants =
        List.of(
            AppealJointRevisionDefendantDTO.builder()
                .appeal(appealDTO)
                .appealStatus(AppealStatusDTO.builder().value("zulässig").build())
                .build());
    var jointRevisionPlaintiffs =
        List.of(
            AppealJointRevisionPlaintiffDTO.builder()
                .appeal(appealDTO)
                .appealStatus(AppealStatusDTO.builder().value("unbegründet").build())
                .build());
    var nzbDefendants =
        List.of(
            AppealNzbDefendantDTO.builder()
                .appeal(appealDTO)
                .appealStatus(AppealStatusDTO.builder().value("sonstiges").build())
                .build());
    var nzbPlaintiffs =
        List.of(
            AppealNzbPlaintiffDTO.builder()
                .appeal(appealDTO)
                .appealStatus(AppealStatusDTO.builder().value("keine Angabe").build())
                .build());
    appealDTO.setAppellants(appellants);
    appealDTO.setRevisionDefendantStatuses(revisionDefendants);
    appealDTO.setRevisionPlaintiffStatuses(revisionPlaintiffs);
    appealDTO.setJointRevisionDefendantStatuses(jointRevisionDefendants);
    appealDTO.setJointRevisionPlaintiffStatuses(jointRevisionPlaintiffs);
    appealDTO.setNzbDefendantStatuses(nzbDefendants);
    appealDTO.setNzbPlaintiffStatuses(nzbPlaintiffs);

    Appeal appeal = AppealTransformer.transformToDomain(appealDTO);

    assertThat(appeal).isNotNull();
    assertThat(appeal.id()).isEqualTo(id);
    assertThat(appeal.appellants())
        .hasSize(1)
        .extracting(Appellant::value)
        .containsExactly("Kläger");
    assertThat(appeal.revisionDefendantStatuses())
        .hasSize(1)
        .extracting(AppealStatus::value)
        .containsExactly("unzulässig");
    assertThat(appeal.revisionPlaintiffStatuses())
        .hasSize(1)
        .extracting(AppealStatus::value)
        .containsExactly("begründet");
    assertThat(appeal.jointRevisionDefendantStatuses())
        .hasSize(1)
        .extracting(AppealStatus::value)
        .containsExactly("zulässig");
    assertThat(appeal.jointRevisionPlaintiffStatuses())
        .hasSize(1)
        .extracting(AppealStatus::value)
        .containsExactly("unbegründet");
    assertThat(appeal.nzbDefendantStatuses())
        .hasSize(1)
        .extracting(AppealStatus::value)
        .containsExactly("sonstiges");
    assertThat(appeal.nzbPlaintiffStatuses())
        .hasSize(1)
        .extracting(AppealStatus::value)
        .containsExactly("keine Angabe");
    assertThat(appeal.appealWithdrawal()).isEqualTo(AppealWithdrawal.JA);
    assertThat(appeal.pkhPlaintiff()).isEqualTo(PkhPlaintiff.JA);
  }

  @Test
  void transformToDomain_withoutLists_shouldPutEmptyLists() {
    var id = UUID.randomUUID();
    AppealDTO appealDTO =
        AppealDTO.builder()
            .id(id)
            .appealWithdrawal(AppealWithdrawal.JA)
            .pkhPlaintiff(PkhPlaintiff.JA)
            .build();

    Appeal appeal = AppealTransformer.transformToDomain(appealDTO);

    assertThat(appeal).isNotNull();
    assertThat(appeal.id()).isEqualTo(id);
    assertThat(appeal.appellants()).hasSize(0);
    assertThat(appeal.revisionDefendantStatuses()).hasSize(0);
    assertThat(appeal.revisionPlaintiffStatuses()).hasSize(0);
    assertThat(appeal.jointRevisionDefendantStatuses()).hasSize(0);
    assertThat(appeal.jointRevisionPlaintiffStatuses()).hasSize(0);
    assertThat(appeal.nzbDefendantStatuses()).hasSize(0);
    assertThat(appeal.nzbPlaintiffStatuses()).hasSize(0);
    assertThat(appeal.appealWithdrawal()).isEqualTo(AppealWithdrawal.JA);
    assertThat(appeal.pkhPlaintiff()).isEqualTo(PkhPlaintiff.JA);
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> generateDecisionDTOBuilder() {
    return DecisionDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }
}
