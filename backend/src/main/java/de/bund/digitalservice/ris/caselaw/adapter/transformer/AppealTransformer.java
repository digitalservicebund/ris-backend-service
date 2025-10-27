package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealAppellantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealAppellantId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealJointRevisionDefendantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealJointRevisionDefendantId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealJointRevisionPlaintiffDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealJointRevisionPlaintiffId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealNzbDefendantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealNzbDefendantId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealNzbPlaintiffDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealNzbPlaintiffId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealRevisionDefendantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealRevisionDefendantId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealRevisionPlaintiffDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealRevisionPlaintiffId;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppellantDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appeal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for transforming LegalForce objects between DTOs (Data Transfer Objects) and domain
 * objects.
 */
public class AppealTransformer {

  private AppealTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms an Appeal (Rechtsmittel) domain object into it's database representation
   *
   * @param appeal (Rechtsmittel) domain object to be transformed.
   * @return A database representation of an appeal domain object.
   */
  public static AppealDTO transformToDTO(DecisionDTO currentDTO, Appeal appeal) {
    if (appeal == null) {
      return null;
    }
    if (appeal.appellants().isEmpty()
        && appeal.revisionDefendantStatuses().isEmpty()
        && appeal.revisionPlaintiffStatuses().isEmpty()
        && appeal.jointRevisionDefendantStatuses().isEmpty()
        && appeal.jointRevisionPlaintiffStatuses().isEmpty()
        && appeal.nzbDefendantStatuses().isEmpty()
        && appeal.nzbPlaintiffStatuses().isEmpty()
        && appeal.appealWithdrawal() == null
        && appeal.pkhPlaintiff() == null) {
      return null;
    }

    AppealDTO.AppealDTOBuilder builder;

    if (currentDTO.getAppeal() != null) {
      builder = currentDTO.getAppeal().toBuilder();
    } else {
      builder = AppealDTO.builder().id(appeal.id());
    }

    builder.appealWithdrawal(appeal.appealWithdrawal());
    builder.pkhPlaintiff(appeal.pkhPlaintiff());

    AppealDTO appealDTO = builder.build();

    addAppellants(appeal, appealDTO);
    addRevisionDefendantStatuses(appeal, appealDTO);
    addRevisionPlaintiffStatuses(appeal, appealDTO);
    addJointRevisionDefendantStatuses(appeal, appealDTO);
    addJointRevisionPlaintiffStatuses(appeal, appealDTO);
    addNzbDefendantStatuses(appeal, appealDTO);
    addNzbPlaintiffStatuses(appeal, appealDTO);

    return appealDTO;
  }

  private static void addAppellants(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.appellants() == null || appeal.appellants().isEmpty()) {
      appealDTO.setAppellants(Collections.emptyList());
      return;
    }
    List<AppealAppellantDTO> appealAppellants = new ArrayList<>();
    for (int i = 0; i < appeal.appellants().size(); i++) {
      var appellant = appeal.appellants().get(i);
      appealAppellants.add(
          AppealAppellantDTO.builder()
              .primaryKey(new AppealAppellantId(appeal.id(), appellant.id()))
              .appeal(appealDTO)
              .appellant(AppellantDTO.builder().id(appellant.id()).value(appellant.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setAppellants(appealAppellants);
  }

  private static void addRevisionDefendantStatuses(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.revisionDefendantStatuses() == null
        || appeal.revisionDefendantStatuses().isEmpty()) {
      appealDTO.setRevisionDefendantStatuses(Collections.emptyList());
      return;
    }
    List<AppealRevisionDefendantDTO> revisionDefendantStatuses = new ArrayList<>();
    for (int i = 0; i < appeal.revisionDefendantStatuses().size(); i++) {
      var status = appeal.revisionDefendantStatuses().get(i);
      revisionDefendantStatuses.add(
          AppealRevisionDefendantDTO.builder()
              .primaryKey(new AppealRevisionDefendantId(appeal.id(), status.id()))
              .appeal(appealDTO)
              .appealStatus(AppealStatusDTO.builder().id(status.id()).value(status.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setRevisionDefendantStatuses(revisionDefendantStatuses);
  }

  private static void addRevisionPlaintiffStatuses(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.revisionPlaintiffStatuses() == null
        || appeal.revisionPlaintiffStatuses().isEmpty()) {
      appealDTO.setRevisionPlaintiffStatuses(Collections.emptyList());
      return;
    }
    List<AppealRevisionPlaintiffDTO> revisionPlaintiffStatuses = new ArrayList<>();
    for (int i = 0; i < appeal.revisionPlaintiffStatuses().size(); i++) {
      var status = appeal.revisionPlaintiffStatuses().get(i);
      revisionPlaintiffStatuses.add(
          AppealRevisionPlaintiffDTO.builder()
              .primaryKey(new AppealRevisionPlaintiffId(appeal.id(), status.id()))
              .appeal(appealDTO)
              .appealStatus(AppealStatusDTO.builder().id(status.id()).value(status.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setRevisionPlaintiffStatuses(revisionPlaintiffStatuses);
  }

  private static void addJointRevisionDefendantStatuses(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.jointRevisionDefendantStatuses() == null
        || appeal.jointRevisionDefendantStatuses().isEmpty()) {
      appealDTO.setJointRevisionDefendantStatuses(Collections.emptyList());
      return;
    }
    List<AppealJointRevisionDefendantDTO> jointRevisionDefendantStatuses = new ArrayList<>();
    for (int i = 0; i < appeal.jointRevisionDefendantStatuses().size(); i++) {
      var status = appeal.jointRevisionDefendantStatuses().get(i);
      jointRevisionDefendantStatuses.add(
          AppealJointRevisionDefendantDTO.builder()
              .primaryKey(new AppealJointRevisionDefendantId(appeal.id(), status.id()))
              .appeal(appealDTO)
              .appealStatus(AppealStatusDTO.builder().id(status.id()).value(status.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setJointRevisionDefendantStatuses(jointRevisionDefendantStatuses);
  }

  private static void addJointRevisionPlaintiffStatuses(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.jointRevisionPlaintiffStatuses() == null
        || appeal.jointRevisionPlaintiffStatuses().isEmpty()) {
      appealDTO.setJointRevisionPlaintiffStatuses(Collections.emptyList());
      return;
    }
    List<AppealJointRevisionPlaintiffDTO> jointRevisionPlaintiffStatuses = new ArrayList<>();
    for (int i = 0; i < appeal.jointRevisionPlaintiffStatuses().size(); i++) {
      var status = appeal.jointRevisionPlaintiffStatuses().get(i);
      jointRevisionPlaintiffStatuses.add(
          AppealJointRevisionPlaintiffDTO.builder()
              .primaryKey(new AppealJointRevisionPlaintiffId(appeal.id(), status.id()))
              .appeal(appealDTO)
              .appealStatus(AppealStatusDTO.builder().id(status.id()).value(status.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setJointRevisionPlaintiffStatuses(jointRevisionPlaintiffStatuses);
  }

  private static void addNzbDefendantStatuses(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.nzbDefendantStatuses() == null || appeal.nzbDefendantStatuses().isEmpty()) {
      appealDTO.setNzbDefendantStatuses(Collections.emptyList());
      return;
    }
    List<AppealNzbDefendantDTO> nzbDefendantStatuses = new ArrayList<>();
    for (int i = 0; i < appeal.nzbDefendantStatuses().size(); i++) {
      var status = appeal.nzbDefendantStatuses().get(i);
      nzbDefendantStatuses.add(
          AppealNzbDefendantDTO.builder()
              .primaryKey(new AppealNzbDefendantId(appeal.id(), status.id()))
              .appeal(appealDTO)
              .appealStatus(AppealStatusDTO.builder().id(status.id()).value(status.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setNzbDefendantStatuses(nzbDefendantStatuses);
  }

  private static void addNzbPlaintiffStatuses(Appeal appeal, AppealDTO appealDTO) {
    if (appeal.nzbPlaintiffStatuses() == null || appeal.nzbPlaintiffStatuses().isEmpty()) {
      appealDTO.setNzbPlaintiffStatuses(Collections.emptyList());
      return;
    }
    List<AppealNzbPlaintiffDTO> nzbPlaintiffStatuses = new ArrayList<>();
    for (int i = 0; i < appeal.nzbPlaintiffStatuses().size(); i++) {
      var status = appeal.nzbPlaintiffStatuses().get(i);
      nzbPlaintiffStatuses.add(
          AppealNzbPlaintiffDTO.builder()
              .primaryKey(new AppealNzbPlaintiffId(appeal.id(), status.id()))
              .appeal(appealDTO)
              .appealStatus(AppealStatusDTO.builder().id(status.id()).value(status.value()).build())
              .rank(i + 1)
              .build());
    }
    appealDTO.setNzbPlaintiffStatuses(nzbPlaintiffStatuses);
  }

  /**
   * Transforms an AppealDTO (database entity) into an Appeal (Rechtsmittel) domain object.
   *
   * @param dto A database representation of an Appeal domain object, to be transformed.
   * @return The transformed Appeal domain object.
   */
  public static Appeal transformToDomain(AppealDTO dto) {
    if (dto == null) {
      return null;
    }

    return new Appeal(
        dto.getId(),
        dto.getAppellants().stream()
            .map(
                appellant ->
                    new Appellant(
                        appellant.getAppellant().getId(), appellant.getAppellant().getValue()))
            .toList(),
        dto.getRevisionDefendantStatuses().stream()
            .map(
                status ->
                    new AppealStatus(
                        status.getAppealStatus().getId(), status.getAppealStatus().getValue()))
            .toList(),
        dto.getRevisionPlaintiffStatuses().stream()
            .map(
                status ->
                    new AppealStatus(
                        status.getAppealStatus().getId(), status.getAppealStatus().getValue()))
            .toList(),
        dto.getJointRevisionDefendantStatuses().stream()
            .map(
                status ->
                    new AppealStatus(
                        status.getAppealStatus().getId(), status.getAppealStatus().getValue()))
            .toList(),
        dto.getJointRevisionPlaintiffStatuses().stream()
            .map(
                status ->
                    new AppealStatus(
                        status.getAppealStatus().getId(), status.getAppealStatus().getValue()))
            .toList(),
        dto.getNzbDefendantStatuses().stream()
            .map(
                status ->
                    new AppealStatus(
                        status.getAppealStatus().getId(), status.getAppealStatus().getValue()))
            .toList(),
        dto.getNzbPlaintiffStatuses().stream()
            .map(
                status ->
                    new AppealStatus(
                        status.getAppealStatus().getId(), status.getAppealStatus().getValue()))
            .toList(),
        dto.getAppealWithdrawal(),
        dto.getPkhPlaintiff());
  }
}
