package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;

public class FileNumberTransformer {

  public static FileNumberDTO enrichDTO(FileNumberDTO fileNumberDTO, String fileNumber) {
    fileNumberDTO.setFileNumber(fileNumber);
    return fileNumberDTO;
  }
}
