package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalEditionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegalPeriodicalEditionTransformer {
  private LegalPeriodicalEditionTransformer() {}

  public static LegalPeriodicalEdition transformToDomain(
      LegalPeriodicalEditionDTO legalPeriodicalEditionDTO) {
    if (legalPeriodicalEditionDTO == null) {
      return null;
    }

    // 1. Alle DTOs in eine temporäre Liste sammeln
    List<Object> allDtos = new ArrayList<>();
    if (legalPeriodicalEditionDTO.getReferences() != null) {
      allDtos.addAll(legalPeriodicalEditionDTO.getReferences());
    }
    if (legalPeriodicalEditionDTO.getPassiveUliCitations() != null) {
      allDtos.addAll(legalPeriodicalEditionDTO.getPassiveUliCitations());
    }

    // 2. Die DTOs nach ihrem editionRank sortieren
    // Da wir zwei verschiedene Klassen haben, müssen wir den Rank "manuell" abgreifen
    allDtos.sort(
        Comparator.comparing(
            dto -> {
              if (dto instanceof ReferenceDTO r) return r.getEditionRank();
              if (dto instanceof PassiveCitationUliDTO u) return u.getEditionRank();
              return Integer.MAX_VALUE;
            },
            Comparator.nullsLast(Comparator.naturalOrder())));

    // 3. Jetzt in der richtigen Reihenfolge transformieren
    List<Reference> sortedReferences =
        allDtos.stream()
            .map(
                dto -> {
                  if (dto instanceof ReferenceDTO r)
                    return ReferenceTransformer.transformToDomain(r);
                  if (dto instanceof PassiveCitationUliDTO u)
                    return PassiveCitationUliTransformer.transformToDomain(u);
                  return null;
                })
            .filter(Objects::nonNull)
            .toList();

    return LegalPeriodicalEdition.builder()
        .id(legalPeriodicalEditionDTO.getId())
        .createdAt(legalPeriodicalEditionDTO.getCreatedAt())
        .legalPeriodical(
            LegalPeriodicalTransformer.transformToDomain(
                legalPeriodicalEditionDTO.getLegalPeriodical()))
        .name(legalPeriodicalEditionDTO.getName())
        .prefix(legalPeriodicalEditionDTO.getPrefix())
        .suffix(legalPeriodicalEditionDTO.getSuffix())
        .references(sortedReferences)
        .build();
  }

  public static LegalPeriodicalEditionDTO transformToDTO(
      LegalPeriodicalEdition legalPeriodicalEdition) {
    if (legalPeriodicalEdition == null) {
      return null;
    }

    return LegalPeriodicalEditionDTO.builder()
        .id(legalPeriodicalEdition.id() != null ? legalPeriodicalEdition.id() : UUID.randomUUID())
        .createdAt(
            legalPeriodicalEdition.createdAt() != null
                ? legalPeriodicalEdition.createdAt()
                : LocalDate.now())
        .legalPeriodical(
            LegalPeriodicalTransformer.transformToDTO(legalPeriodicalEdition.legalPeriodical()))
        .name(legalPeriodicalEdition.name())
        .prefix(legalPeriodicalEdition.prefix())
        .suffix(legalPeriodicalEdition.suffix())
        .build();
  }
}
