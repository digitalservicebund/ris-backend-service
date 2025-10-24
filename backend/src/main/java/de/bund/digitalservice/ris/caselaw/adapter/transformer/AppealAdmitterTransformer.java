package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealAdmitterDTO;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmitter;

public class AppealAdmitterTransformer {

  private AppealAdmitterTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  public static AppealAdmitterDTO transformToDTO(AppealAdmitter appealAdmitter) {
    if (appealAdmitter == null) {
      return null;
    }

    return switch (appealAdmitter) {
      case FG -> AppealAdmitterDTO.FG;
      case BFH -> AppealAdmitterDTO.BFH;
    };
  }

  public static AppealAdmitter transformToDomain(AppealAdmitterDTO dto) {
    if (dto == null) {
      return null;
    }

    return switch (dto) {
      case FG -> AppealAdmitter.FG;
      case BFH -> AppealAdmitter.BFH;
    };
  }
}
