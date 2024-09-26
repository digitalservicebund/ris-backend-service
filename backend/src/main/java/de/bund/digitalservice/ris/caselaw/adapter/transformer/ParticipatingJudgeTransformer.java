package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ParticipatingJudgeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.ParticipatingJudge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for transforming objects between DTOs (Data Transfer Objects) {@link
 * ParticipatingJudgeDTO} and domain objects {@link ParticipatingJudge}.
 */
public class ParticipatingJudgeTransformer {

  private ParticipatingJudgeTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a List of {@link ParticipatingJudge} domain objects into a List of {@link
   * ParticipatingJudgeDTO} (Data Transfer Object).
   *
   * @param judges The {@link ParticipatingJudge} to be transformed.
   * @return The list {@link List<ParticipatingJudgeDTO>} of DTO representing the transformed domain
   *     objects.
   */
  public static List<ParticipatingJudgeDTO> transformToDTO(List<ParticipatingJudge> judges) {
    if (judges == null || judges.isEmpty()) {
      return Collections.emptyList();
    }
    List<ParticipatingJudge> participatingJudges = judges.stream().distinct().toList();
    List<ParticipatingJudgeDTO> result = new ArrayList<>();
    for (int i = 0; i < participatingJudges.size(); i++) {
      result.add(
          ParticipatingJudgeDTO.builder()
              .id(participatingJudges.get(i).id())
              .name(participatingJudges.get(i).name())
              .referencedOpinions(participatingJudges.get(i).referencedOpinions())
              .rank(i + 1L)
              .build());
    }
    return result;
  }

  /**
   * Transforms a List of {@link ParticipatingJudgeDTO} (Data Transfer Object) into a List of {@link
   * ParticipatingJudge} domain objects.
   *
   * @param participatingJudges The list of {@link ParticipatingJudgeDTO} to be transformed.
   * @return The list {@link List<ParticipatingJudge>} of domain objects representing the DTOs.
   */
  public static List<ParticipatingJudge> transformToDomain(
      List<ParticipatingJudgeDTO> participatingJudges) {
    return participatingJudges.stream()
        .map(
            participatingJudge ->
                ParticipatingJudge.builder()
                    .id(participatingJudge.getId())
                    .name(participatingJudge.getName())
                    .referencedOpinions(participatingJudge.getReferencedOpinions())
                    .build())
        .distinct()
        .toList();
  }
}
